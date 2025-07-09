package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.manager.utils.UpgradeUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RequestMapping("/admin/upload")
@RestController
public class UpgradeController {

    // 系统配置常量
    private static final String UPLOAD_DIR = "/home/metoo/";
    private static final String FRONTEND_DIR = "/opt/nrsm/";
    private static final String BACKEND_JAR = "/opt/nrsm/nrsm/nrsm.jar";

    @PostMapping("/upgrade")
    public ResponseEntity<String> uploadUpgrade(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Upload failed: File is empty");
        }

        try {
            // 保存上传文件
            String tempZip = saveUploadedFile(file);
            File zipFile = new File(tempZip);

            // 检测升级包类型
            UpgradeType type = detectUpgradeType(zipFile);

            // 执行升级
            return executeUpgrade(type, zipFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upgrade failed: " + e.getMessage());
        }
    }

    private String saveUploadedFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_upgrade.zip";
        File target = new File(UPLOAD_DIR + fileName);
        file.transferTo(target);
        return target.getAbsolutePath();
    }

    private UpgradeType detectUpgradeType(File zipFile) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.getName().endsWith(".jar")) {
                    return UpgradeType.BACKEND;
                }
                if (entry.getName().startsWith("www/") || entry.getName().endsWith("index.html")) {
                    return UpgradeType.FRONTEND;
                }
            }
        }
        return UpgradeType.UNKNOWN;
    }

    private ResponseEntity<String> executeUpgrade(UpgradeType type, File zipFile) throws Exception {
        switch (type) {
            case FRONTEND:
                UpgradeUtils.unzip(zipFile, FRONTEND_DIR);
                return ResponseEntity.ok("Frontend upgrade success");

            case BACKEND:
                File newJar = UpgradeUtils.extractJar(zipFile);
                UpgradeUtils.replaceJar(newJar, BACKEND_JAR);
                UpgradeUtils.runCommand("systemctl restart nrsm");
                return ResponseEntity.ok("Backend upgrade success. Service restarted");

            default:
                return ResponseEntity.badRequest().body("Unknown package type");
        }
    }

    enum UpgradeType { FRONTEND, BACKEND, UNKNOWN }
}