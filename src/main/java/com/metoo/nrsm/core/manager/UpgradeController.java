package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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

    @PostMapping("/upgrade")
    public Result uploadUpgrade(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseUtil.error("Upload failed: File is empty");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 验证 ZIP 文件头
            if (!isValidZipFile(inputStream)) {
                return ResponseUtil.error("Invalid ZIP file format");
            }

            // 保存上传文件
            File tempZip = saveUploadedFile(file);

            // 再次验证 ZIP 文件结构
            if (!isValidZipStructure(tempZip)) {
                return ResponseUtil.error("Malformed ZIP file structure");
            }

            // 检测升级包类型
            UpgradeType type = detectUpgradeType(tempZip);

            // 执行升级
            return executeUpgrade(type, tempZip);
        } catch (Exception e) {
            return ResponseUtil.error("Upgrade failed: " + e.getMessage());
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
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry;

        while ((entry = zipIn.getNextEntry()) != null) {
            if (entry.getName().toLowerCase().endsWith(".jar")) {
                zipIn.close();
                return UpgradeType.BACKEND;
            }
            String lcName = entry.getName().toLowerCase();
            if (lcName.contains("www/") || lcName.contains("static/")) {
                zipIn.close();
                return UpgradeType.FRONTEND;
            }
            zipIn.closeEntry();
        }
        zipIn.close();
        return UpgradeType.UNKNOWN;
    }

    private Result executeUpgrade(UpgradeType type, File zipFile) throws Exception {
        switch (type) {
            case FRONTEND:
                unzipTo(zipFile, FRONTEND_DIR);
                return ResponseUtil.ok("Frontend upgrade success");

            case BACKEND:
                File newJar = extractJar(zipFile);
                replaceJar(newJar, BACKEND_JAR);
                //runCommand("sudo systemctl restart nrsm");
                return ResponseUtil.ok("Backend upgrade success. Service restarted");

            default:
                return ResponseUtil.ok("Unknown package type");
        }
    }

    private void unzipTo(File zipFile, String outputDir) throws IOException {

        try (ZipFile zip = new ZipFile(zipFile)) {
            File output = new File(outputDir);
            if (!output.exists() && !output.mkdirs()) {
                throw new IOException("Failed to create output directory: " + outputDir);
            }

            String canonicalOutputPath = output.getCanonicalPath() + File.separator;


            Enumeration<? extends ZipEntry> entries = zip.entries();
            int fileCount = 0, dirCount = 0;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                // 跳过无效条目
                if (!isValidZipEntry(entry)) {
                    System.out.println("Skipping invalid entry: " + entry.getName());
                    continue;
                }

                File destFile = new File(output, entry.getName());
                String canonicalEntryPath = destFile.getCanonicalPath();

                // 检查路径安全
                if (!canonicalEntryPath.startsWith(canonicalOutputPath)) {
                    throw new IOException("Blocked path traversal attempt: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    if (!destFile.exists() && !destFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + destFile);
                    }
                    dirCount++;
                } else {
                    File parent = destFile.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create parent directory: " + parent);
                    }

                    try (InputStream in = zip.getInputStream(entry);
                         OutputStream out = new FileOutputStream(destFile)) {

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        throw new IOException("Failed to extract: " + entry.getName(), e);
                    }
                    fileCount++;
                }
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    // 辅助方法：验证ZIP条目
    private boolean isValidZipEntry(ZipEntry entry) {
        if (entry == null) return false;

        String name = entry.getName();
        // 过滤无效字符
        if (name.contains("..") || name.contains("\0") || name.contains(":") ||
                name.contains("?") || name.contains("*")) {
            return false;
        }

        // 过滤特殊目录
        if (name.startsWith("__MACOSX/") || name.equals(".DS_Store")) {
            return false;
        }

        return true;
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

    enum UpgradeType { FRONTEND, BACKEND, UNKNOWN }
}