package com.metoo.nrsm.core.manager.utils;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class UpgradeUtils {

    public static void unzip(File zipFile, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();
        
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File file = new File(outputDir, entry.getName());
                
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    new File(file.getParent()).mkdirs();
                    Files.copy(zipIn, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public static File extractJar(File zipFile) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.getName().endsWith(".jar")) {
                    File tempJar = File.createTempFile("nrsm", ".jar");
                    Files.copy(zipIn, tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return tempJar;
                }
            }
        }
        throw new IOException("No JAR file found in package");
    }

    public static void replaceJar(File newJar, String targetPath) throws IOException {
        File target = new File(targetPath);
        File backup = new File(targetPath + ".bak");
        
        // 备份原文件
        if (target.exists()) {
            Files.move(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        // 原子替换
        Files.move(newJar.toPath(), target.toPath());
    }

    public static void runCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed: " + command);
        }
    }
}