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
import com.metoo.nrsm.entity.BackupSql;
import io.swagger.annotations.ApiOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * 数据库维护
 */
@RequestMapping("/admin/backup/db")
@RestController
public class BackupSqlController {

    @Autowired
    private IBackupSqlService backupSqlService;

    // 系统剩余空间

    // 上传

    // 下载

    // 备份

    // 还原

    // 还原状态

    // 重置

    //

    // 命令行备份数据库
//    @GetMapping
//    public boolean backupDB(String fileName) {
//        // 命令行
//        fileName += ".sql";
//
//        String savePath = Global.DBPATH;
//
//        File saveFile = new File(Global.DBPATH);
//
//        if (!saveFile.exists()) {
//            saveFile.mkdirs();
//        }
//
//        if (!savePath.endsWith(File.separator)) {
//            savePath = savePath + File.separator;
//        }
//
//        //拼接命令行的命令
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("mysqldump").append(" --opt").append(" -h").append("nmap-mysql");
//        stringBuilder.append(" --user=").append("root").append(" --password=").append("metoo89745000")
//                .append(" --lock-all-tables=true");
//        stringBuilder.append(" --result-file=").append(savePath + fileName).append(" --default-character-set=utf8 ")
//                .append("nmap");
//        // 追加表名
//
//        stringBuilder.append(" rsms_terminal rsms_device ");
//
//        try {
//            System.out.println(stringBuilder.toString());
//            //调用外部执行exe文件的javaAPI
//            Process process = Runtime.getRuntime().exec(stringBuilder.toString());
//            if (process.waitFor() == 0) {// 0 表示线程正常终止。
//                return true;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    @PostMapping("/list")
    public Object list(@RequestBody(required = false) BackupSqlDTO dto){
        if(dto == null){
            dto = new BackupSqlDTO();
        }
        Page<BackupSql> page = this.backupSqlService.selectObjConditionQuery(dto);
        if(page.getResult().size() > 0){
            return ResponseUtil.ok(new PageInfo<BackupSql>(page));
        }
        return ResponseUtil.ok();
    }

    /**
     * 备份
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
            if (!process1.waitFor(10, TimeUnit.SECONDS)) {
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
            if (!process2.waitFor(10, TimeUnit.SECONDS)) {
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



    public static void main(String[] args) {
        try {
            // 创建命令数组
            String[] command = {
                    "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
                    "-uroot",
                    "-h",
                    "127.0.0.1",
                    "-p123456",
                    "metoo_nrsm_local"
            };

            // 创建 ProcessBuilder 对象并设置命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);

            // 将输入重定向到文件
            processBuilder.redirectInput(new File("C:\\Users\\Administrator\\Desktop\\backup\\db\\test\\metoo_nrsm_local.sql"));

            // 启动进程
            Process process = processBuilder.start();

            // 读取进程输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待进程结束并获取退出状态
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

//    @ApiOperation("恢复备份")
//    @PutMapping("/recover/{id}")
//    public Object recoverSBackup(@PathVariable Long id){
//        BackupSql backupSql = this.backupSqlService.selectObjById(id);
//        if(backupSql != null){
//            try {
//                // 构建MySQL导入命令
//                String recover = this.recover(backupSql.getName());
//                // 创建ProcessBuilder对象
//
//                // 创建命令数组
//                StringBuilder sb = new StringBuilder();
//                sb.append("C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe");
//                sb.append(" -h"+ "127.0.0.1");
//                sb.append(" -P"+ "3306");
//                sb.append(" -u"+ "root");
//                sb.append(" -p"+ "123456");
//                sb.append(" "+ "metoo_nrsm_local");
//                String[] command = {
//                        "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
//                        "-uroot",
//                        "-p123456",
//                        "metoo_nrsm_local"
//                };
//
//                String[] command2 = {
//                        "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
//                        "-uroot",
//                        "-h",
//                        "127.0.0.1",
//                        "-p123456",
//                        "metoo_nrsm_local"
//                };
//
//                // 创建 ProcessBuilder 对象并设置命令
////                ProcessBuilder processBuilder = new ProcessBuilder(sb.toString());
//
//                ProcessBuilder processBuilder = new ProcessBuilder(command2);
//
////                ProcessBuilder processBuilder = new ProcessBuilder(recover);
//
//                processBuilder.redirectErrorStream(true);
//                // 将输入重定向到文件
//                String dbPath = getDbPath(backupSql.getName());
//                String fileName = getDbName();
//                processBuilder.redirectInput(new File(dbPath + fileName));
//                // 启动进程
//                Process process = processBuilder.start();
//
//                // 读取进程的输出流并打印
//                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//
//                // 启动一个单独的线程来读取进程的错误流
//                Thread errorReaderThread = new Thread(() -> {
//                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//                    String errorLine;
//                    try {
//                        while ((errorLine = errorReader.readLine()) != null) {
//                            System.err.println(errorLine);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//                errorReaderThread.start();
//
//                // 等待进程执行完毕
//                int exitCode = process.waitFor();
//                System.out.println("MySQL command executed, exit code: " + exitCode);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return ResponseUtil.badArgument();
//    }


    @ApiOperation("恢复备份")
    @PutMapping("/recover/{id}")
    public Object recoverSBackup(@PathVariable Long id) {
        BackupSql backupSql = this.backupSqlService.selectObjById(id);
        if (backupSql != null) {
            try {
                // 构建 MySQL 导入命令
                String recover = this.recover(backupSql.getName());
                String[] command = recover.split("\\s+");
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true); // 合并标准错误流

                // 启动进程
                Process process = processBuilder.start();

                // 读取进程的输出流
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                // 等待进程执行完毕
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return ResponseUtil.ok("恢复成功");
                } else {
                    return handleProcessError(process);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseUtil.error("恢复过程中发生异常: " + e.getMessage());
            }
        }
        return ResponseUtil.badArgument();
    }

    private Object handleProcessError(Process process) {
        StringBuilder errorMsg = new StringBuilder();
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorMsg.append(errorLine).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseUtil.error("恢复失败，错误信息: " + errorMsg.toString());
    }


    @GetMapping("/get/size")
    public String size() {
        return this.getAvailableSpace("/opt/nmap/resource/db/");
    }

    /*public String getSize(String path) {
        try {
            Process p = Runtime.getRuntime().exec("du -sh " + path);

            // 等待进程完成
            if (p.waitFor() == 0) {
                StringBuilder builder = new StringBuilder();

                // 使用 try-with-resources 自动管理资源
                try (InputStream is = p.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append(System.lineSeparator());
                    }
                }

                // 返回处理后的结果
                String result = builder.toString().trim();
                if (!result.isEmpty() && result.contains("/")) {
                    return result.substring(0, result.indexOf("/")).trim();
                }
                return result;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            e.printStackTrace();
        }
        return "";
    }*/
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
            size = getSizeWindows(dbPath+File.separator + fileName);
        }else{
            size = getSizeLinux(dbPath+File.separator + fileName);
        }
        return size;
    }

    public String  getSizeWindows(String path) {
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
        String savePath = Global.DBPATH + "/" + backupName;
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
    public Object uploadConfig(@RequestParam(value = "file", required = false) MultipartFile file, String name){
            if(file != null){
                boolean accessory = this.fileUtil.uploadFile(file, name,  ".sql", Global.DBPATH);
                if(accessory){
                    return ResponseUtil.ok();
                }else{
                    return ResponseUtil.error();
                }
            }
        return ResponseUtil.badArgument();
    }

    // 下载
    @GetMapping("/down/{id}")
    public Object down(@PathVariable Long id, HttpServletResponse response){
        BackupSql backupSql = this.backupSqlService.selectObjById(id);
        if(backupSql != null){
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

    public String recover(String name){
        String dbPath = getDbPath();
        String recover = "";
        if ("dev".equals(Global.env)) {
            recover = recoverWindows(dbPath, name);
        }else{
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
//        sb.append(" C:\\Users\\Administrator\\Desktop\\backup\\db\\test\\metoo_nrsm_local.sql");
//        return sb.toString();
//    }
    public String recoverWindows(String dbPath, String dbName){
        StringBuilder command = new StringBuilder();
        command.append("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe").append(" ");
        command.append("--user=").append("root").append(" ");
        command.append("--password=").append("xsl101410").append(" ");
        command.append("nsrm").append(" ");
        command.append("-e \"source ").append(dbPath + dbName + ".sql").append("\"");
        return command.toString();
    }

    public String recoverLinux(String dbPath, String dbName){
        StringBuilder sb = new StringBuilder();
//        sb.append("mysql");
//        sb.append(" -h"+ "127.0.0.1");
//        sb.append(" -P"+ "3306");
//        sb.append(" -u"+ "root");
//        sb.append(" -p"+ "metoo89745000");
//        sb.append(" "+ "nrsm" + " --default-character-set=utf8  < ");
//        sb.append(dbPath + fileName + ".sql");

        StringBuilder command = new StringBuilder();
        command.append("mysql").append(" ");
        command.append("--user=").append("root").append(" ");
        command.append("--password=").append("metoo89745000").append(" ");
        command.append("nrsm").append(" ");
        command.append("-e \"source ").append(dbPath + dbName + ".sql").append("\"");

        return command.toString();
    }

    public String dump(String name){
        String dbPath = getDbPath();
        String dump = "";
        if ("dev".equals(Global.env)) {
            dump = dumpWindows(dbPath, name);
        }else{
            dump = dumpLinux(dbPath, name);
        }
        return dump;
    }

    public String dump1(String name){
        String dbPath = getDbPath1();
        String dump = "";
        if ("dev".equals(Global.env)) {
            dump = dumpWindows1(dbPath, name);
        }else{
            dump = dumpLinux1(dbPath, name);
        }
        return dump;
    }

    public String dumpWindows(String savePath, String fileName){
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
                .append("xsl101410")
                .append(" --lock-all-tables=true");
        stringBuilder
                .append(" --result-file=")
                .append(savePath + fileName)
                .append(".sql")
                .append(" --default-character-set=utf8 ")
                .append("nsrm");
        return stringBuilder.toString();
    }

    public String dumpWindows1(String savePath, String fileName){
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
                .append("xsl101410")
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

    public String dumpLinux(String savePath, String fileName){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("mysqldump")
                .append(" --opt")
                .append(" -h")
                .append(" 127.0.0.1");
        stringBuilder
                .append(" --user=")
                .append(" root")
                .append(" --password=")
                .append("metoo89745000")
                .append(" --lock-all-tables=true");
        stringBuilder
                .append(" --result-file=")
                .append(savePath + fileName)
                .append(".sql")
                .append(" --default-character-set=utf8 ")
                .append("nrsm");
        return stringBuilder.toString();
    }

    public String dumpLinux1(String savePath, String fileName){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("mysqldump")
                .append(" --opt")
                .append(" -h")
                .append(" 127.0.0.1");
        stringBuilder
                .append(" --user=")
                .append(" root")
                .append(" --password=")
                .append("metoo89745000")
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

    public String getDbPath(){
        // 命令行
        String dbPath = "";
        if ("dev".equals(Global.env)){
            dbPath = Global.DBPATHLOCAL;
        }else{
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

    public String getDbPath1(){
        // 命令行
        String dbPath = "";
        if ("dev".equals(Global.env)){
            dbPath = Global.LICENSEPATHLOCAL;
        }else{
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

    // 获取文件大小
    /**
     * 使用 MySQL 命令行工具或客户端查询表的大小：使用 SHOW TABLE STATUS 命令可以查看每个表的数据大小和索引大小，然后将它们相加得到表的总大小。这样你就可以比较导出前后的大小。
     *
     * 压缩导出的 SQL 文件：尝试使用压缩工具（如 gzip、zip 等）对导出的 SQL 文件进行压缩，这通常可以显著减小文件大小。然后再与数据库的大小进行比较。
     *
     * 检查导出的 SQL 文件是否包含完整的数据：确保导出的 SQL 文件包含了数据库中所有的数据。有时候导出的 SQL 文件可能只包含了表结构而没有数据，这会导致文件大小明显减小。
     *
     * 检查表引擎：不同的表引擎（如 InnoDB、MyISAM 等）可能会影响数据在磁盘上的存储方式，从而影响导出的文件大小。确保导出前后使用的是相同的表引擎。
     *
     * 检查数据类型和编码：一些数据类型和字符集（如 UTF-8、UTF-16 等）可能会占用更多的空间。确保导出前后数据类型和字符集的一致性。
     */
    @Test
    public void getDatabaseSize() {
        String url = "jdbc:mysql://localhost:3306/nsrm";
        String user = "root";
        String password = "xsl101410";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            String query = "SELECT SUM(data_length + index_length) AS Size " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = 'nsrm';";

            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                long size = resultSet.getLong("Size");
                System.out.println("数据库大小：" + size + " bytes");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取系统大小
    @Test
    public void getSpace() {
        // 指定文件路径
        File file = new File("D:\\java");

        // 查询磁盘空间
        long totalSpace = file.getTotalSpace(); // 总空间
        long freeSpace = file.getFreeSpace();   // 可用空间

        // 打印磁盘空间信息
        System.out.println("总空间: " + convertFileSize(totalSpace));
        System.out.println("可用空间: " + convertFileSize(freeSpace));
    }

    @Test
    public void getWindowsSpace(){
        try {
            // 执行 wmic 命令并获取输出
            Process process = Runtime.getRuntime().exec("wmic logicaldisk get FreeSpace");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // 解析输出
            String line;
            long availableSpace = 0;
            while ((line = reader.readLine()) != null) {
                // 跳过标题行
                if (line.startsWith("FreeSpace")) continue;

                // 解析输出中的可用空间
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length > 0) {
                    try {
                        availableSpace += Long.parseLong(tokens[0]);
                    } catch (NumberFormatException e) {
                        // 解析失败
                    }
                }
            }

            System.out.println("可用磁盘空间：" + convertFileSize(availableSpace));

            // 关闭 BufferedReader
            reader.close();

            // 等待进程执行完毕
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void getLinuxSpace(){
        String url = "jdbc:mysql://localhost:3306/nsrm";
        String user = "root";
        String password = "xsl101410";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            String query = "SELECT SUM(data_length + index_length) AS Size " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = 'nsrm';";

            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                long size = resultSet.getLong("Size");
                System.out.println("数据库大小：" + convertFileSize(size));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
