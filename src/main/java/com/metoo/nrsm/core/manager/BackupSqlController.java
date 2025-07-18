package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.BackupSqlDTO;
import com.metoo.nrsm.core.service.IBackupSqlService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.file.DownLoadFileUtil;
import com.metoo.nrsm.core.utils.file.FileUtil;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.BackupSql;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 数据库维护
 */
@RequestMapping("/admin/backup/db")
@RestController
public class BackupSqlController {

    @Autowired
    private IBackupSqlService backupSqlService;

    @Value("${spring.datasource.password}")
    private String DB_PASSWORD;

    @PostMapping("/list")
    public Object list(@RequestBody(required = false) BackupSqlDTO dto) {
        if (dto == null) {
            dto = new BackupSqlDTO();
        }
        Page<BackupSql> page = this.backupSqlService.selectObjConditionQuery(dto);
        if (page.getResult().size() > 0) {
            return ResponseUtil.ok(new PageInfo<BackupSql>(page));
        }
        return ResponseUtil.ok();
    }

    /**
     * 备份
     *
     * @param name
     * @return
     */
    @GetMapping
    public Object backupDB(String name) {
        name = name + "-" + DateTools.currentTimeMillis();
        // 备份命令
        String dump1 = this.dump1(name); // 获取第一个备份命令
        String dump = this.dump(name); // 获取第二个备份命令

        // 执行第一个备份
        try {
            Process process1 = Runtime.getRuntime().exec(dump1);

            // 等待进程完成，设定超时
            if (!process1.waitFor(20, TimeUnit.SECONDS)) {
                process1.destroy(); // 超时则终止进程
                return ResponseUtil.error("备份超时");
            }

            if (process1.exitValue() != 0) {
                // 输出返回的错误信息
                StringBuilder mes = new StringBuilder();
                try (BufferedReader error = new BufferedReader(new InputStreamReader(process1.getErrorStream()))) {
                    String tmp;
                    while ((tmp = error.readLine()) != null) {
                        mes.append(tmp).append("\n");
                    }
                }
                return ResponseUtil.error("备份失败: " + mes.toString());
            }

            // 执行第二个备份
            Process process2 = Runtime.getRuntime().exec(dump);

            // 等待进程完成，设定超时
            if (!process2.waitFor(60 * 3, TimeUnit.SECONDS)) {
                process2.destroy(); // 超时则终止进程
                return ResponseUtil.error("备份超时");
            }

            if (process2.exitValue() != 0) {
                // 输出返回的错误信息
                StringBuilder mes = new StringBuilder();
                try (BufferedReader error = new BufferedReader(new InputStreamReader(process2.getErrorStream()))) {
                    String tmp;
                    while ((tmp = error.readLine()) != null) {
                        mes.append(tmp).append("\n");
                    }
                }
                return ResponseUtil.error("备份失败: " + mes.toString());
            }

            // 创建记录
            BackupSql backupSql = this.backupSqlService.selectObjByName(name);
            if (backupSql == null) {
                backupSql = new BackupSql();
            }
            backupSql.setName(name);
            backupSql.setSize(this.getDbSize(name));
            this.backupSqlService.save(backupSql);

            return ResponseUtil.ok("备份成功");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseUtil.error("IO异常: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            return ResponseUtil.error("操作被中断");
        }
    }


    @GetMapping("/backup")
    public Object backupDB1(String name) {
        name = name + "-" + DateTools.currentTimeMillis();

        // 根据环境选择脚本文件
        String scriptPath1;
        String scriptPath2;
        String backupDir = null;
        String backupDir2 = null;
        if ("test".equals(Global.env)) {
            backupDir = Global.LICENSEPATHLOCAL;
            backupDir2 = Global.DBPATHLOCAL;
            scriptPath1 = Global.DBSCRIPTPATHLOCAL; // Windows 脚本路径
            scriptPath2 = Global.DBSCRIPTPATHLOCAL2; // Windows 脚本路径
        } else {
            backupDir = Global.LICENSEPATH;
            backupDir2 = Global.DBPATH;
            scriptPath1 = Global.DBSCRIPTPATH; // Linux 脚本路径
            scriptPath2 = Global.DBSCRIPTPATH2; // Linux 脚本路径
        }

        // 执行第一个备份脚本
        // 创建 ProcessBuilder
        ProcessBuilder processBuilder1 = new ProcessBuilder(scriptPath1, name, backupDir);
        processBuilder1.redirectErrorStream(true); // 合并错误流和输出流

        try {
            Process process1 = processBuilder1.start();

            // 等待进程完成，设定超时
            if (!process1.waitFor(30, TimeUnit.SECONDS)) {
                process1.destroy(); // 超时则终止进程
                return ResponseUtil.error("备份超时");
            }

            // 读取输出结果
            StringBuilder output1 = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process1.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output1.append(line).append("\n");
                }
            }

            // 检查返回值
            if (process1.exitValue() != 0) {
                return ResponseUtil.error("备份失败: " + output1.toString());
            }

            // 执行第二个备份脚本
            ProcessBuilder processBuilder2 = new ProcessBuilder(scriptPath2, name, backupDir2);
            processBuilder2.redirectErrorStream(true); // 合并错误流和输出流

            Process process2 = processBuilder2.start();

            // 等待进程完成，设定超时
            if (!process2.waitFor(60 * 3, TimeUnit.SECONDS)) {
                process2.destroy(); // 超时则终止进程
                return ResponseUtil.error("备份超时");
            }

            // 读取输出结果
            StringBuilder output2 = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process2.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output2.append(line).append("\n");
                }
            }

            // 检查返回值
            if (process2.exitValue() != 0) {
                return ResponseUtil.error("备份失败: " + output2.toString());
            }

            // 创建记录（如果需要）
            BackupSql backupSql = this.backupSqlService.selectObjByName(name);
            if (backupSql == null) {
                backupSql = new BackupSql();
            }
            backupSql.setName(name);
            backupSql.setSize(this.getDbSize(name));
            this.backupSqlService.save(backupSql);

            return ResponseUtil.ok("备份成功");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseUtil.error("IO异常: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            return ResponseUtil.error("操作被中断");
        }
    }


    // 解析大小字符串并转换为字节
    private long parseSize(String size) {
        String[] parts = size.trim().split(" ");
        if (parts.length == 2) {
            long value = Long.parseLong(parts[0]);
            String unit = parts[1].toUpperCase();
            return convertToBytes(value, unit);
        }
        return 0; // 如果格式不正确，返回0
    }

    // 将文件大小单位转换为字节
    public static long convertToBytes(long fileSize, String unit) {
        switch (unit.toUpperCase()) {
            case "KB":
                return fileSize * 1024; // 将 KB 转换为字节
            case "MB":
                return fileSize * 1024 * 1024; // 将 MB 转换为字节
            case "GB":
                return fileSize * 1024 * 1024 * 1024; // 将 GB 转换为字节
            case "BYTES":
            default: // 默认情况下返回原始字节数
                return fileSize;
        }
    }


    @ApiOperation("恢复备份")
    @PutMapping("/recover/{id}")
    public Object recoverSBackup(@PathVariable Long id) {
        BackupSql backupSql = this.backupSqlService.selectObjById(id);
        if (backupSql == null) {
            return ResponseUtil.badArgument();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Path configFile = null;
        Path logFile = null;
        final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        try {
            // 1. 预处理 - 命令构建与安全校验
            configFile = createSecuredConfigFile();
            String sqlFilePath = Global.DBPATH+"/" + backupSql.getName() + ".sql";

            // 检查备份文件是否存在
            if (!Files.exists(Paths.get(sqlFilePath))) {
                return ResponseUtil.error("备份文件不存在: " + sqlFilePath);
            }

            // 2. 命令构建（避免路径空格问题）
            String[] command = buildRecoverCommand(sqlFilePath, configFile);

            // 3. 创建唯一日志文件（添加时间戳避免并发冲突）
            logFile = Paths.get("/opt/nrsm/nrsm/resource/db_recover_" + backupSql.getName() + "_" + System.currentTimeMillis() + ".log");

            Path finalLogFile = logFile;
            Future<Integer> future = executor.submit(() -> {
                try {
                    // 4. 进程构建与执行
                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    processBuilder.redirectErrorStream(true);
                    processBuilder.redirectOutput(finalLogFile.toFile());

                    Process process = processBuilder.start();
                    long pid = getProcessId(process);
                    // 5. 双重超时控制机制
                    final long timeoutMs = 2 * 60 * 60 * 1000; // 2小时
                    final long endTime = System.currentTimeMillis() + timeoutMs;

                    while (true) {
                        try {
                            // 每10秒检查一次
                            if (process.waitFor(10, TimeUnit.SECONDS)) {
                                return process.exitValue();
                            }

                            // 检查超时
                            if (System.currentTimeMillis() > endTime) {
                                destroyProcessTree(pid, isWindows);
                                throw new TimeoutException("恢复超时强制终止");
                            }
                        } catch (InterruptedException e) {
                            // 处理中断
                        }
                    }
                } catch (IOException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
            });

            // 6. 外部超时控制（比进程多5分钟）
            int exitCode = future.get(125, TimeUnit.MINUTES);

            if (exitCode == 0) {
                Files.deleteIfExists(logFile); // 成功时清理日志
                return ResponseUtil.ok("恢复成功");
            } else {
                return handleProcessError(logFile);
            }
        } catch (TimeoutException e) {
            return ResponseUtil.error("恢复超时，已终止");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            String msg = (cause != null) ? cause.getMessage() : e.getMessage();
            return ResponseUtil.error("执行错误: " + msg);
        } catch (Exception e) {
            return ResponseUtil.error("恢复异常: " + e.getMessage());
        } finally {
            // 7. 确保资源清理
            if (configFile != null) {
                try { Files.deleteIfExists(configFile); } catch (IOException ignored) {}
            }
            executor.shutdownNow();
        }
    }
    private long getProcessId(Process process) {
        try {
            if (process.getClass().getName().equals("java.lang.UNIXProcess") ||
                    process.getClass().getName().equals("java.lang.ProcessImpl")) {

                Field pidField = process.getClass().getDeclaredField("pid");
                pidField.setAccessible(true);
                return (long) pidField.get(process);
            }
        } catch (Exception e) {
            // 反射失败
        }
        return -1;
    }


    private String[] buildRecoverCommand(String sqlFilePath, Path configFile) {
        // 安全构建命令数组（避免路径空格问题）
        return new String[]{
                "mysql",
                "--defaults-file=" + configFile.toString(),
                "--max_allowed_packet=1G",
                "--quick",
                "--compress",
                "-e",
                String.format("SET autocommit=0; " +
                                "SET unique_checks=0; " +
                                "SET foreign_key_checks=0; " +
                                "SET sql_log_bin=0; " +
                                "SOURCE %s; " +
                                "COMMIT;",
                        sqlFilePath.replace("'", "\\'")), // 转义单引号
                "nrsm"  // 目标数据库名
        };
    }

    private void destroyProcessTree(long pid, boolean isWindows) {
        try {
            if (pid == -1) {
                return; // 无法获取PID
            }

            if (isWindows) {
                // Windows 终止进程树
                new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(pid)).start();
            } else {
                // Linux/Mac 终止进程组
                new ProcessBuilder("pkill", "-P", String.valueOf(pid)).start();
            }
        } catch (IOException e) {
            // 终止失败时记录
            try {
                Files.write(Paths.get("/tmp/nrsm_kill_error.log"),
                        ("无法终止进程: " + pid).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ignored) {}
        }
    }

    private Object handleProcessError(Path logFile) {
        try {
            // 读取日志尾部（最后200行）
            List<String> lines = Files.readAllLines(logFile);
            int startIndex = Math.max(0, lines.size() - 200);
            String errorTail = String.join("\n", lines.subList(startIndex, lines.size()));

            // 识别常见错误
            if (errorTail.contains("ERROR 2006 (HY000) at line") ||
                    errorTail.contains("MySQL server has gone away")) {
                return ResponseUtil.error("数据库连接中断，请增加max_allowed_packet");
            } else if (errorTail.contains("Lock wait timeout")) {
                return ResponseUtil.error("数据库锁等待超时，请稍后重试");
            }

            return ResponseUtil.error("恢复失败，错误信息:\n" + errorTail);
        } catch (IOException e) {
            return ResponseUtil.error("读取错误日志失败: " + e.getMessage());
        }
    }


    @GetMapping("/get/size")
    public Result size() {
        String availableSpace = this.getAvailableSpace("/opt/nrsm/nrsm/resource/db/");
        return ResponseUtil.ok(availableSpace);
    }

    public String getAvailableSpace(String path) {
        try {
            // 使用 df 命令获取可用空间
            Process p = Runtime.getRuntime().exec("df -h --output=avail " + path);

            // 等待进程完成
            if (p.waitFor() == 0) {
                StringBuilder builder = new StringBuilder();

                // 使用 try-with-resources 自动管理资源
                try (InputStream is = p.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    // 跳过第一行（标题行）
                    reader.readLine();
                    // 读取可用空间
                    if ((line = reader.readLine()) != null) {
                        return line.trim(); // 返回可用空间
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            e.printStackTrace();
        }
        return "";
    }


    public String getDbSize(String name) {
        String dbPath = getDbPath();
        String size = "";
        String fileName = name + ".sql";
        if ("dev".equals(Global.env)) {
            size = getSizeWindows(dbPath + File.separator + fileName);
        } else {
            size = getSizeLinux(dbPath + File.separator + fileName);
        }
        return size;
    }

    public String getSizeWindows(String path) {
        // 指定文件路径
        String filePath = path;

        // 创建 File 对象
        File file = new File(filePath);

        // 检查文件是否存在
        if (file.exists()) {
            // 获取文件大小（以字节为单位）
            long fileSize = file.length();

            String fileSizeStr = convertFileSize(fileSize);

            // 打印文件大小
            System.out.println("文件大小: " + fileSize + " 字节");

            System.out.println("文件大小: " + fileSizeStr);
            return fileSizeStr;
        } else {
            System.out.println("文件不存在！");
        }
        return "0 byte";
    }

    public String getSizeLinux(String path) {
        // 指定文件路径（在 Linux 中使用绝对路径）
        String filePath = path;

        // 创建 File 对象
        File file = new File(filePath);

        // 检查文件是否存在
        if (file.exists()) {
            // 获取文件大小（以字节为单位）
            long fileSize = file.length();

            // 转换文件大小为更大的单位
            String fileSizeStr = convertFileSize(fileSize);
            // 打印文件大小
            System.out.println("文件大小: " + fileSizeStr);
            return fileSizeStr;
        } else {
            System.out.println("文件不存在！");
        }
        return "0 byte";
    }

    // 将文件大小转换为更大的单位（KB、MB、GB）
    public static String convertFileSize(long fileSize) {
        if (fileSize < 1024) {
            return fileSize + " byte";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }


    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id) {
        BackupSql backupSql = this.backupSqlService.selectObjById(id);
        if (backupSql != null) {
            int result = this.backupSqlService.delete(backupSql.getId());
            if (result >= 1) {
                return deleteBackupFile(backupSql.getName());
            }
            return ResponseUtil.error("备份记录删除失败");
        }
        return ResponseUtil.badArgument("无效的备份 ID");
    }

    private Object deleteBackupFile(String backupName) {
        String savePath = null;
        if ("dev".equals(Global.env)) {
            savePath = Global.DBPATHLOCAL + "/" + backupName + ".sql";
        } else {
            savePath = Global.DBPATH + "/" + backupName + ".sql";
        }
        File saveFile = new File(savePath);
        if (saveFile.exists()) {
            try {
                FileSystemUtils.deleteRecursively(saveFile);
                return ResponseUtil.ok("备份文件删除成功");
            } catch (Exception e) {
                e.printStackTrace(); // 可以替换为日志记录
                return ResponseUtil.error("备份文件删除失败: " + e.getMessage());
            }
        }
        return ResponseUtil.ok("备份文件不存在");
    }

    @Autowired
    private FileUtil fileUtil;

    // 上传
    @ApiOperation("上传")
    @RequestMapping("/upload")
    public Object uploadConfig(@RequestParam(value = "file", required = false) MultipartFile file, String name) {
        if (file != null) {
            boolean accessory = this.fileUtil.uploadFile(file, name, ".sql", Global.DBPATH);
            if (accessory) {
                return ResponseUtil.ok();
            } else {
                return ResponseUtil.error();
            }
        }
        return ResponseUtil.badArgument();
    }

    // 下载
    @GetMapping("/down/{id}")
    public Object down(@PathVariable Long id, HttpServletResponse response) {
        BackupSql backupSql = this.backupSqlService.selectObjById(id);
        if (backupSql != null) {
            String path = Global.DBPATH + File.separator + backupSql.getName() + File.separator + Global.DBNAME + ".sql";
            File file = new File(path);
            if (file.getParentFile().exists()) {
                boolean flag = DownLoadFileUtil.downloadZip(file, response);
                if (flag) {
                    return ResponseUtil.ok();
                } else {
                    return ResponseUtil.error("文件下载失败");
                }
            } else {
                return ResponseUtil.badArgument("文件下载失败");
            }
        }
        return ResponseUtil.badArgument();

    }

    public String recover(String name) {
        String dbPath = getDbPath();
        String recover = "";
        if ("test".equals(Global.env)) {
            recover = recoverWindows(dbPath, name);
        } else {
            recover = recoverLinux(dbPath, name);
        }
        return recover;
    }

    //    public String recoverWindows(){
//        StringBuilder sb = new StringBuilder();
//        sb.append("C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe");
//        sb.append(" -h"+ "127.0.0.1");
//        sb.append(" -P"+ "3306");
//        sb.append(" -u"+ "root");
//        sb.append(" -p"+ "123456");
//        sb.append(" "+ "metoo_nrsm_local");
//
//        sb.append(" <");
//        sb.append(" C:\\Users\\Administrator\\Desktop\\backup\\db\\TestAbstrack\\metoo_nrsm_local.sql");
//        return sb.toString();
//    }
    public String recoverWindows(String dbPath, String dbName) {
        StringBuilder command = new StringBuilder();
        command.append("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe").append(" ");
        command.append("--user=").append("root").append(" ");
        command.append("--password=").append(DB_PASSWORD).append(" ");
        command.append("nsrm").append(" ");
        command.append("-e \"source ").append(dbPath + dbName + ".sql").append("\"");
        return command.toString();
    }

    public String recoverLinux(String dbPath, String dbName) {
        try {
            // 1. 创建安全配置文件（避免密码暴露在命令中）
            Path configFile = createSecuredConfigFile();

            // 2. 构建SQL文件完整路径
            Path sqlFile = Paths.get(dbPath, dbName + ".sql");

            // 3. 构建优化的恢复命令（Java 8 兼容版本）
            return String.format(
                    "mysql --defaults-file=%s %s --max_allowed_packet=1G --quick " +
                            "--compress -e \"SET autocommit=0; " +
                            "SET unique_checks=0; " +
                            "SET foreign_key_checks=0; " +
                            "SET sql_log_bin=0; " +
                            "SOURCE %s; " +
                            "COMMIT;\"",
                    configFile.toString(),
                    "nrsm",  // 目标数据库名
                    sqlFile.toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("恢复命令构建失败: " + e.getMessage());
        }
    }

    // 安全配置文件创建方法（Java 8 兼容）
    private Path createSecuredConfigFile() throws IOException {
        // 创建临时配置文件
        Path configPath = Files.createTempFile("mysql_config_", ".cnf");

        // 配置内容（包含密码）
        String configContent = String.format(
                "[client]%n" +
                        "user=root%n" +
                        "password=%s%n" +
                        "loose-local-infile=1", // 启用本地文件加载（安全模式）
                DB_PASSWORD
        );

        Files.write(configPath, configContent.getBytes());

        // 设置权限（仅所有者可读写）
        try {
            // Java 8 兼容的权限设置
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(configPath, perms);
        } catch (UnsupportedOperationException ignored) {
            // Windows系统跳过权限设置
        }

        // JVM退出时自动删除
        configPath.toFile().deleteOnExit();

        return configPath;
    }

    public String dump(String name) {
        String dbPath = getDbPath();
        String dump = "";
        if ("test".equals(Global.env)) {
            dump = dumpWindows(dbPath, name);
        } else {
            dump = dumpLinux(dbPath, name);
        }
        return dump;
    }

    public String dump1(String name) {
        String dbPath = getDbPath1();
        String dump = "";
        if ("test".equals(Global.env)) {
            dump = dumpWindows1(dbPath, name);
        } else {
            dump = dumpLinux1(dbPath, name);
        }
        return dump;
    }

    public String dumpWindows(String savePath, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump")
                .append(" --opt")
                .append(" -h")
                .append(" 127.0.0.1");
        stringBuilder
                .append(" --user=")
                .append("root")
                .append(" --password=")
                .append(DB_PASSWORD)
                .append(" --lock-all-tables=true");
        stringBuilder
                .append(" --result-file=")
                .append(savePath + fileName)
                .append(".sql")
                .append(" --default-character-set=utf8 ")
                .append("nsrm");
        return stringBuilder.toString();
    }

    public String dumpWindows1(String savePath, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump")
                .append(" --opt")
                .append(" -h")
                .append(" 127.0.0.1");
        stringBuilder
                .append(" --user=")
                .append("root")
                .append(" --password=")
                .append(DB_PASSWORD)
                .append(" --lock-all-tables=true");
        stringBuilder
                .append(" --result-file=")
                .append(savePath + fileName)
                .append(".sql")
                .append(" --default-character-set=utf8 ")
                .append("nsrm");
        // 追加表名
        stringBuilder.append(" metoo_license");
        return stringBuilder.toString();
    }

    public String dumpLinux(String savePath, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("mysqldump")
                .append(" --opt")
                .append(" -h")
                .append(" 127.0.0.1");
        stringBuilder
                .append(" --user=")
                .append("root")
                .append(" --password=")
                .append(DB_PASSWORD)
                .append(" --lock-all-tables=true");
        stringBuilder
                .append(" --result-file=")
                .append(savePath + fileName)
                .append(".sql")
                .append(" --default-character-set=utf8 ")
                .append("nrsm");
        return stringBuilder.toString();
    }

    public String dumpLinux1(String savePath, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("mysqldump")
                .append(" --opt")
                .append(" -h")
                .append(" 127.0.0.1");
        stringBuilder
                .append(" --user=")
                .append("root")
                .append(" --password=")
                .append(DB_PASSWORD)
                .append(" --lock-all-tables=true");
        stringBuilder
                .append(" --result-file=")
                .append(savePath + fileName)
                .append(".sql")
                .append(" --default-character-set=utf8 ")
                .append("nrsm");
        // 追加表名
        stringBuilder.append(" metoo_license");
        return stringBuilder.toString();
    }

    public String getDbPath() {
        // 命令行
        String dbPath = "";
        if ("test".equals(Global.env)) {
            dbPath = Global.DBPATHLOCAL;
        } else {
            dbPath = Global.DBPATH;
        }
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            dbFile.mkdirs();
        }
        if (!dbPath.endsWith(File.separator)) {
            dbPath = dbPath + File.separator;
        }
        return dbPath;
    }

    public String getDbPath1() {
        // 命令行
        String dbPath = "";
        if ("test".equals(Global.env)) {
            dbPath = Global.LICENSEPATHLOCAL;
        } else {
            dbPath = Global.LICENSEPATH;
        }
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            dbFile.mkdirs();
        }
        if (!dbPath.endsWith(File.separator)) {
            dbPath = dbPath + File.separator;
        }
        return dbPath;
    }
    

}
