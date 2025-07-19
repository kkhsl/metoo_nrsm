package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.vo.Result;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@RequestMapping("/admin/upload")
@RestController
public class UpgradeController {

    // 系统配置常量
    @Value("${upload.dir}")
    private String UPLOAD_DIR;
    //private static final String UPLOAD_DIR = "C:\\Users\\leo\\Desktop\\update\\home";

    @Value("${fronted.dir}")
    private String FRONTEND_DIR;
    //private static final String FRONTEND_DIR = "C:\\Users\\leo\\Desktop\\update\\opt\\nrsm";

    @Value("${backend.dir}")
    private String BACKEND_JAR;
    //private static final String BACKEND_JAR = "C:\\Users\\leo\\Desktop\\update\\opt\\nrsm\\nrsm\\nrsm.jar";


    // 备份目录
    private static final String BACKUP_DIR = "backup";
    private String currentBackupDir = null;

    private static final int MAX_BACKUPS = 2; // 最大保留备份数

    @PostMapping("/upgrade")
    public Result uploadUpgrade(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseUtil.error("上传失败：文件为空");
        }

        File tempZip = null;
        try (InputStream inputStream = file.getInputStream()) {
            // 验证 ZIP 文件头
            if (!isValidZipFile(inputStream)) {
                return ResponseUtil.error("无效的ZIP文件格式");
            }

            // 保存上传文件
            tempZip = saveUploadedFile(file);

            // 再次验证 ZIP 文件结构
            if (!isValidZipStructure(tempZip)) {
                return ResponseUtil.error("非法的ZIP文件结构");
            }

            // 清理旧备份
            cleanupOldBackups();

            // 创建当前备份目录
            currentBackupDir = createBackupDir();

            // 检测升级包类型
            UpgradeType type = detectUpgradeType(tempZip);

            // 执行升级
            return executeUpgrade(type, tempZip);
        } catch (Exception e) {
            // 尝试回滚
            try {
                rollbackUpgrade();
            } catch (Exception ex) {
                return ResponseUtil.error("升级失败且回滚失败: " + e.getMessage() + "; 回滚错误: " + ex.getMessage());
            }
            return ResponseUtil.error("升级失败，已回滚: " + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempZip != null && !tempZip.delete()) {
                tempZip.deleteOnExit();
            }
        }
    }

    private void cleanupOldBackups() {
        File backupRoot = new File(FRONTEND_DIR, BACKUP_DIR);
        if (!backupRoot.exists() || !backupRoot.isDirectory()) {
            return;
        }

        File[] backups = backupRoot.listFiles(File::isDirectory);
        if (backups == null || backups.length <= MAX_BACKUPS) {
            return;
        }

        // 按修改时间排序（最旧的在前面）
        Arrays.sort(backups, Comparator.comparingLong(File::lastModified));

        // 删除超出数量限制的旧备份
        for (int i = 0; i < backups.length - MAX_BACKUPS; i++) {
            try {
                deleteDirectory(backups[i]);
            } catch (Exception e) {
                // 记录错误但继续执行
            }
        }
    }

    private String createBackupDir() {
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        return BACKUP_DIR + "/backup-" + timestamp;
    }

    private void backupFrontend() throws IOException {
        File backupDir = new File(FRONTEND_DIR, currentBackupDir);
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            throw new IOException("无法创建备份目录: " + backupDir.getAbsolutePath());
        }

        // 1. 创建 www 目录的备份文件夹
        File wwwBackupDir = new File(backupDir, "www");
        if (!wwwBackupDir.exists() && !wwwBackupDir.mkdirs()) {
            throw new IOException("无法创建 www 备份目录");
        }

        // 2. 仅备份 www 目录
        File wwwSource = new File(FRONTEND_DIR, "www");

        // 检查 www 目录是否存在
        if (!wwwSource.exists()) {
            throw new IOException("前端 www 目录不存在: " + wwwSource.getAbsolutePath());
        }

        // 3. 复制整个 www 目录
        copyFileOrDirectory(wwwSource, wwwBackupDir);
    }

    private void copyFileOrDirectory(File source, File dest) throws IOException {
        if (source.isDirectory()) {
            if (!dest.exists() && !dest.mkdirs()) {
                throw new IOException("无法创建目录: " + dest.getAbsolutePath());
            }

            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyFileOrDirectory(file, new File(dest, file.getName()));
                }
            }
        } else {
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(dest)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }

    private void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!dir.delete()) {
            throw new IOException("无法删除: " + dir.getAbsolutePath());
        }
    }

    private File saveUploadedFile(MultipartFile file) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_upgrade.zip";
        File target = new File(uploadDir, fileName);

        try (InputStream in = file.getInputStream();
             OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return target;
    }

    private boolean isValidZipFile(InputStream is) throws IOException {
        // ZIP 文件头签名：PK\x03\x04
        byte[] header = new byte[4];
        if (is.read(header) < 4) return false;
        return (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04);
    }

    private boolean isValidZipStructure(File zipFile) {
        try {
            return runCommand("unzip -tq " + zipFile.getAbsolutePath()).contains("No errors detected");
        } catch (Exception e) {
            return false;
        }
    }

    private UpgradeType detectUpgradeType(File zipFile) throws IOException {
        boolean hasFrontendDir = false;
        boolean hasBackendJar = false;

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();

            while (entries.hasMoreElements() && !(hasFrontendDir && hasBackendJar)) {
                ZipArchiveEntry entry = entries.nextElement();
                String name = entry.getName();

                // 只检查顶级目录和文件
                String baseName = getBaseName(name);

                // 判断前端目录
                if (baseName.equals("www") || baseName.equals("frontend")) {
                    // 确保是目录而不是文件
                    if (name.endsWith("/") || entry.isDirectory()) {
                        hasFrontendDir = true;
                    }
                }
                // 判断后端JAR文件
                else if (name.endsWith(".jar") && name.split("/").length == 1) {
                    hasBackendJar = true;
                }
            }
        } catch (Exception e) {
            throw new IOException("MALFORMED");
        }

        // 输出检测结果便于调试
        System.out.println("检测结果: www存在=" + hasFrontendDir + "，jar存在=" + hasBackendJar);

        if (hasFrontendDir && hasBackendJar) {
            return UpgradeType.FULL;
        } else if (hasFrontendDir) {
            return UpgradeType.FRONTEND;
        } else if (hasBackendJar) {
            return UpgradeType.BACKEND;
        } else {
            return UpgradeType.UNKNOWN;
        }
    }

    private String getBaseName(String path) {
        if (path.contains("/")) {
            return path.split("/")[0];
        }
        return path;
    }

    private Result executeUpgrade(UpgradeType type, File zipFile) throws Exception {
        try {
            switch (type) {
                case FRONTEND:
                    // 备份前端
                    backupFrontend();

                    // 执行升级
                    unzipTo(zipFile, FRONTEND_DIR);

                    // 清理更新包
                    zipFile.delete();

                    return ResponseUtil.ok("前端升级成功");

                case BACKEND:
                    // 备份后端
                    File backupJar = new File(BACKEND_JAR + ".bak");
                    if (new File(BACKEND_JAR).exists()) {
                        copyFile(new File(BACKEND_JAR), backupJar);
                    }

                    // 执行升级
                    File newJar = extractJar(zipFile);
                    replaceJar(newJar, BACKEND_JAR);

                    // 异步重启（避免中断HTTP响应）
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000); // 等待响应返回
                            runCommand("sudo systemctl restart nrsm");
                        } catch (Exception e) {
                            // 记录日志
                        }
                    }).start();

                    return ResponseUtil.ok("后端升级成功，服务将在后台重启");
                case FULL:
                    // 备份前后端
                    backupFrontend();
                    backupBackend();

                    // 解压整个升级包
                    File tempDir = createTempDirectory();
                    try {
                        unzipTo(zipFile, tempDir.getAbsolutePath());
                        // 分别升级前后端
                        upgradeFrontendFromDir(tempDir);
                        upgradeBackendFromDir(tempDir);


                        // 异步重启服务
                        asyncRestartService();
                    } finally {
                        deleteDirectory(tempDir);
                    }

                    return ResponseUtil.ok("全栈升级成功，服务将在后台重启");

                default:
                    return ResponseUtil.ok("未知的升级包类型");
            }
        } catch (Exception e) {
            rollbackUpgrade();
            throw e;
        }
    }
    private void backupBackend() throws IOException {
        File backupFile = new File(BACKEND_JAR + ".bak");
        File sourceFile = new File(BACKEND_JAR);

        if (sourceFile.exists()) {
            copyFile(sourceFile, backupFile);
        }
    }
    private File createTempDirectory() throws IOException {
        File tempDir = Files.createTempDirectory("upgrade_tmp_").toFile();
        tempDir.deleteOnExit();
        return tempDir;
    }

    private void upgradeFrontendFromDir(File tempDir) throws IOException {
        File frontendDir = new File(tempDir, "www");
        if (!frontendDir.exists()) {
            frontendDir = new File(tempDir, "www");
        }

        if (!frontendDir.exists()) {
            throw new IOException("在前端目录中未找到升级文件");
        }

        // 复制所有前端文件
        File[] files = frontendDir.listFiles();
        if (files != null) {
            for (File file : files) {
                copyFileOrDirectory(file, new File(FRONTEND_DIR+"/www", file.getName()));
            }
        }
    }

    private void upgradeBackendFromDir(File tempDir) throws IOException {
        File backendDir = new File(tempDir, "");
        if (!backendDir.exists()) {
            backendDir = new File(tempDir, "");
        }

        if (!backendDir.exists()) {
            throw new IOException("在后端目录中未找到升级文件");
        }

        // 查找后端JAR文件
        File[] jarFiles = backendDir.listFiles((d, name) ->
                name.endsWith(".jar") || name.endsWith(".war"));

        if (jarFiles == null || jarFiles.length == 0) {
            throw new IOException("backend目录中未找到jar文件");
        }

        replaceJar(jarFiles[0], BACKEND_JAR);
    }

    private void asyncRestartService() {
        new Thread(() -> {
            try {
                // 等待3秒确保HTTP响应已经返回
                Thread.sleep(3000);
                runCommand("sudo systemctl restart nrsm");
            } catch (Exception e) {
                // 记录日志
            }
        }).start();
    }


    private void rollbackUpgrade() throws IOException, InterruptedException {
        // 回滚前端
        if (currentBackupDir != null) {
            File backupDir = new File(FRONTEND_DIR, currentBackupDir);
            if (backupDir.exists()) {
                // 恢复备份
                File[] files = backupDir.listFiles();
                if (files != null) {
                    File frontend = new File(FRONTEND_DIR);
                    // 清空当前目录
                    Arrays.stream(frontend.listFiles(f -> !f.getName().equals(BACKUP_DIR)))
                            .forEach(File::delete);
                    // 恢复文件
                    for (File file : files) {
                        copyFileOrDirectory(file, new File(frontend, file.getName()));
                    }
                }
            }
        }

        // 回滚后端
        File backupJar = new File(BACKEND_JAR + ".bak");
        if (backupJar.exists()) {
            replaceJar(backupJar, BACKEND_JAR);
            runCommand("sudo systemctl restart nrsm");
        }
    }

    public static void unzipTo(File zipFile, String outputDir) throws IOException {
        File output = new File(outputDir);
        if (!output.exists() && !output.mkdirs()) {
            throw new IOException("Failed to create output directory: " + outputDir);
        }

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                File destFile = new File(output, sanitizeFileName(entry.getName()));

                // 路径安全检查
                if (!destFile.getCanonicalPath().startsWith(output.getCanonicalPath())) {
                    throw new IOException("Invalid ZIP entry path: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    if (!destFile.exists() && !destFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + destFile);
                    }
                } else {
                    // 创建父目录
                    File parent = destFile.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create parent directory");
                    }

                    // 使用临时文件确保原子操作
                    File tempFile = File.createTempFile("unzip_", ".tmp", parent);

                    try (InputStream in = zip.getInputStream(entry);
                         OutputStream out = new FileOutputStream(tempFile)) {
                        IOUtils.copy(in, out);
                    }

                    // 原子移动文件
                    Files.move(tempFile.toPath(), destFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);

                    // 设置文件权限
                    setFilePermissions(destFile);
                }
            }
        } catch (Exception e) {
            throw new IOException("Unzip failed: " + e.getMessage(), e);
        }
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[:*?\"<>|]", "_") // 替换非法字符
                .replace("..", ".")             // 防止路径遍历
                .trim();                        // 去除空白
    }

    private static void setFilePermissions(File file) throws IOException {
        // 设置文件权限为755（rwxr-xr-x）
        if (!file.setReadable(true, false) ||
                !file.setWritable(true, true) ||
                !file.setExecutable(true, false)) {
            throw new IOException("Failed to set permissions for: " + file.getAbsolutePath());
        }
    }




    private File extractJar(File zipFile) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry;

        while ((entry = zipIn.getNextEntry()) != null) {
            if (entry.getName().toLowerCase().endsWith(".jar")) {
                File tempJar = File.createTempFile("nrsm", ".jar");
                tempJar.deleteOnExit(); // 确保临时文件会被删除

                try (FileOutputStream out = new FileOutputStream(tempJar)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zipIn.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }

                zipIn.close();
                return tempJar;
            }
            zipIn.closeEntry();
        }
        zipIn.close();
        throw new IOException("No JAR file found in package");
    }

    private void replaceJar(File newJar, String targetPath) throws IOException {
        File target = new File(targetPath);
        File backup = new File(targetPath + ".bak");

        // 备份原文件
        if (target.exists()) {
            copyFile(target, backup);
        }

        // 移动新文件
        if (!newJar.renameTo(target)) {
            // 如果跨设备移动失败，使用复制方法
            copyFile(newJar, target);
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private String runCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});

        // 读取命令输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // 等待命令完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with exit code " + exitCode + ": " + command);
        }

        return output.toString();
    }

    enum UpgradeType { FRONTEND,FULL, BACKEND, UNKNOWN }
}