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

    @GetMapping
    public Object backupDB(String name) {

        name = name + "-" + DateTools.currentTimeMillis();
        // 备份
        String dump = this.dump(name);

        try {
            //调用外部执行exe文件的javaAPI
            Process process = Runtime.getRuntime().exec(dump);
            process.waitFor(10000, TimeUnit.MILLISECONDS);
            // 0 表示线程正常终止
            if (process.waitFor() == 0) {
                // 创建记录
                BackupSql backupSql = this.backupSqlService.selectObjByName(name);
                if(backupSql == null){
                    backupSql = new BackupSql();
                }
                backupSql.setName(name);
//                backupSql.setSize(this.getSize(Global.DBPATH + "/" + name));

                backupSql.setSize(this.getDbSize(name));
                this.backupSqlService.save(backupSql);
                return ResponseUtil.ok();
            }else{
                //输出返回的错误信息
                StringBuffer mes = new StringBuffer();
                String tmp = "";
                BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while((tmp = error.readLine()) != null){
                    mes.append(tmp + "\n");
                }
                if(mes != null || !"".equals(mes) ){
                    System.out.println("备份成功!==>" + mes.toString());
                }
                error.close();
                return ResponseUtil.error(mes.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseUtil.error();
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
    public Object recoverSBackup(@PathVariable Long id){
        BackupSql backupSql = this.backupSqlService.selectObjById(id);
        if(backupSql != null){
            try {
                // 构建MySQL导入命令
                String recover = this.recover(backupSql.getName());
                // 创建ProcessBuilder对象(使用StringBuilder构建命令时,需将字符串拆分为参数数组)
                String[] command = recover.split("\\s+");
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
                // 将输入重定向到文件
//                String dbPath = getDbPath();
//                processBuilder.redirectInput(new File(dbPath));
                // 启动进程
                Process process = processBuilder.start();

//                Runtime 使用StringBuilder拼接参数，使用此方法执行
//                Process process = Runtime.getRuntime().exec(recover);

                // 读取进程的输出流并打印
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                // 启动一个单独的线程来读取进程的错误流
                Thread errorReaderThread = new Thread(() -> {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String errorLine;
                    try {
                        while ((errorLine = errorReader.readLine()) != null) {
                            System.err.println(errorLine);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                errorReaderThread.start();

                // 等待进程执行完毕
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    return ResponseUtil.ok("恢复成功");
                } else {
                    System.err.println("Import failed with exit code: " + exitCode);
                    return ResponseUtil.error("恢复失败");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseUtil.badArgument();
    }


    @GetMapping("/get/size")
    public String size(){
       return this.getSize("/opt/nmap/resource/db/test");
    }

    public String getSize(String path){
        try {
            Process p = Runtime.getRuntime().exec("du -sh " + path);

            if (p.waitFor() == 0) {// 0 表示线程正常终止

                InputStream is = p.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;

                StringBuilder builder = new StringBuilder();

                while((line = reader.readLine())!= null){

                    builder.append(line);

                }

                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                is.close();

                reader.close();

                p.destroy();

                if (builder.length()==0) {
                    return "";
                } else {
                    String str = builder.substring(0, builder.length() - System.lineSeparator().length());
                    if(str.indexOf("/") > -1){
                        return str.substring(0, str.indexOf("/")).trim();
                    }
                    return builder.substring(0, builder.length() - System.lineSeparator().length());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getDbSize(String name) {
        String dbPath = getDbPath();
        String size = "";
        String fileName = name + ".sql";
        if (Global.env.equals("prod")) {
            size = getSizeLinux(dbPath+File.separator + name);
        }else if("dev".equals(Global.env)){
            size = getSizeWindows(dbPath+File.separator + name);
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
    public Object delete(@PathVariable Long id){
        BackupSql backupSql = this.backupSqlService.selectObjById(id);
        if(backupSql != null){
            int i = this.backupSqlService.delete(backupSql.getId());
            if(i >= 1){
                String savePath = Global.DBPATH + "/" + backupSql.getName();
                File saveFile = new File(savePath);
                if (saveFile.exists()) {
                    try {
                        FileSystemUtils.deleteRecursively(saveFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return ResponseUtil.ok();
            }
            return ResponseUtil.error();
        }
        return ResponseUtil.badArgument();
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
        if (Global.env.equals("prod")) {
            recover = recoverLinux(dbPath, name);
        }else if("dev".equals(Global.env)){
            recover = recoverWindows(dbPath, name);
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
        command.append("C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe").append(" ");
        command.append("--user=").append("root").append(" ");
        command.append("--password=").append("123456").append(" ");
        command.append("metoo_nrsm_local").append(" ");
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
        if (Global.env.equals("prod")) {
            dump = dumpLinux(dbPath, name);
        }else if("dev".equals(Global.env)){
            dump = dumpWindows(dbPath, name);
        }
        return dump;
    }

    public String dumpWindows(String savePath, String fileName){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump")
                .append(" --opt")
                .append(" -h")
                .append(" 127.0.0.1");
        stringBuilder
                .append(" --user=")
                .append("root")
                .append(" --password=")
                .append("123456")
                .append(" --lock-all-tables=true");
        stringBuilder
                .append(" --result-file=")
                .append(savePath + fileName)
                .append(".sql")
                .append(" --default-character-set=utf8 ")
                .append("metoo_nrsm_local");
        // 追加表名
//        stringBuilder.append(" rsms_terminal rsms_device ");
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
                .append(" metoo89745000!")
                .append(" --lock-all-tables=true");
        stringBuilder
                .append(" --result-file=")
                .append(savePath + fileName)
                .append(".sql")
                .append(" --default-character-set=utf8 ")
                .append(" nrsm");
        return stringBuilder.toString();
    }

    public String getDbPath(){
        // 命令行
        String dbPath = "";
        if (Global.env.equals("prod")) {
            dbPath = Global.DBPATH;
        }else if("dev".equals(Global.env)){
            dbPath = Global.DBPATHLOCAL;
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

//    public String getDbPath(String name){
//        // 命令行
//        String dbPath = "";
//
//        if (Global.env.equals("prod")) {
//            dbPath = Global.DBPATH + File.separator + name;
//        }else if("dev".equals(Global.env)){
//            dbPath = Global.DBPATHLOCAL + File.separator + name;
//        }
//
//        File dbFile = new File(dbPath);
//
//        if (!dbFile.exists()) {
//            dbFile.mkdirs();
//        }
//
//        if (!dbPath.endsWith(File.separator)) {
//            dbPath = dbPath + File.separator;
//        }
//        return dbPath;
//    }


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
        String url = "jdbc:mysql://localhost:3306/metoo_nrsm_local";
        String user = "root";
        String password = "123456";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            String query = "SELECT SUM(data_length + index_length) AS Size " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = 'metoo_nrsm_local';";

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
        String url = "jdbc:mysql://localhost:3306/nrsm";
        String user = "root";
        String password = "metoo89745000";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            String query = "SELECT SUM(data_length + index_length) AS Size " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = 'nrsm';";

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
