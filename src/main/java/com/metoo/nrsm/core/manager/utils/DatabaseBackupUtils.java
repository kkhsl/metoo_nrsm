package com.metoo.nrsm.core.manager.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-10 16:41
 */
public class DatabaseBackupUtils {

    public static void main(String[] args) {
        String dbName = "your_database_name";
        String dbUser = "your_database_username";
        String dbPass = "your_database_password";
        String backupPath = "path_to_backup_file.sql";
        String excludedTable = "excluded_table_name";

        try {
            // Connect to the database
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dbName, dbUser, dbPass);

            // Execute backup command
            String backupCommand = "mysqldump --user=" + dbUser + " --password=" + dbPass + " --databases " + dbName + " -r " + backupPath;

            // Execute backup command with excluded table
//            String backupCommand = "mysqldump --user=" + dbUser + " --password=" + dbPass + " --databases " + dbName +
//                    " --ignore-table=" + dbName + "." + excludedTable + " -r " + backupPath;

            Process process = Runtime.getRuntime().exec(backupCommand);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Backup successful!");
            } else {
                System.out.println("Backup failed!");
            }

            if (exitCode == 0) {
                System.out.println("Backup successful!");

                // Move backup file to specified directory
                File backupFile = new File(backupPath);
                File targetDirectory = new File("path_to_target_directory");
                if (!targetDirectory.exists()) {
                    targetDirectory.mkdirs();
                }
                File newLocation = new File(targetDirectory, backupFile.getName());
                backupFile.renameTo(newLocation);

                System.out.println("Backup file saved to: " + newLocation.getAbsolutePath());
            } else {
                System.out.println("Backup failed!");
            }

            // Close the database connection
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void recover() {
        // JDBC连接参数
        String url = "jdbc:mysql://localhost:3306/database_name";
        String user = "username";
        String password = "password";

        // JDBC连接对象
        Connection conn = null;

        try {
            // 注册MySQL驱动程序
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 建立连接
            conn = DriverManager.getConnection(url, user, password);

            // 创建Statement对象
            Statement stmt = conn.createStatement();

            // 读取SQL文件并逐个执行SQL语句
            String sqlFile = "path/to/your/sqlfile.sql";
            try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 执行SQL语句
                    stmt.execute(line);
                }
            }

            // 关闭Statement
            stmt.close();
        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void ssh() {
        try {
            // 构建备份命令
            String backupScript = "./backup.sh"; // 备份脚本的路径
            ProcessBuilder pb = new ProcessBuilder(backupScript);

            // 启动Shell进程
            Process process = pb.start();

            // 等待Shell脚本执行完毕
            int exitCode = process.waitFor();

            // 检查脚本执行结果
            if (exitCode == 0) {
                System.out.println("Backup successful!");
            } else {
                System.out.println("Backup failed!");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
