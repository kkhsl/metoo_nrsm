package com.metoo.nrsm.core.utils.file;


import com.metoo.nrsm.core.utils.Global;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataFileWrite {


    public static void main(String[] args) {
        // 要写入的字符串数据
        String data = "TestAbstrack";


//        String uploadDir = Paths.get("").toAbsolutePath().toString() + File.separator + "files" + File.separator + System.currentTimeMillis();
        // 项目所在目录的相对路径
        Path projectDir = Paths.get("").toAbsolutePath();

        // files/upload 文件夹的路径
        Path uploadDir = projectDir.resolve("files/upload");

        // 确保目录存在
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            System.err.println("创建目录失败: " + e.getMessage());
            return;
        }

        // 要写入的文件路径
        Path filePath = uploadDir.resolve("unencrypt.txt");

        // 将字符串数据写入文件
        try {
            Files.write(filePath, data.getBytes());
            System.out.println("数据写入成功: " + filePath);
        } catch (IOException e) {
            System.err.println("写入文件失败: " + e.getMessage());
        }
    }


    public static void write(String args, String fileName) {
        // 要写入的字符串数据
        String data = args;


//        String uploadDir = Paths.get("").toAbsolutePath().toString() + File.separator + "files" + File.separator + System.currentTimeMillis();
        // 项目所在目录的相对路径
        Path projectDir = Paths.get("").toAbsolutePath();

        // files/upload 文件夹的路径
//        Path uploadDir = projectDir.resolve("files/upload");
        Path uploadDir = projectDir.resolve(Global.encrypt_path);

        // 确保目录存在
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            System.err.println("创建目录失败: " + e.getMessage());
            return;
        }

        // 要写入的文件路径
//        Path filePath = uploadDir.resolve("unencrypt.txt");
        Path filePath = uploadDir.resolve(fileName);

        // 将字符串数据写入文件

        // 将字符串数据写入文件
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), StandardCharsets.UTF_8))) {
        // 使用 BufferedWriter 和 OutputStreamWriter 写入文件
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8))) {


            // 使用 Gson 库来格式化 JSON 字符串
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            Object jsonObject = gson.fromJson(jsonString, Object.class);
//            String formattedJson = gson.toJson(jsonObject);

            writer.write(data);
            System.out.println("数据写入成功: " + filePath);
        } catch (IOException e) {
            System.err.println("写入文件失败: " + e.getMessage());
        }

//        try {
//            Files.write(filePath, data.getBytes());
//            System.out.println("数据写入成功: " + filePath);
//        } catch (IOException e) {
//            System.err.println("写入文件失败: " + e.getMessage());
//        }
    }


    public static void clearFile() {
        String filePath = Global.os_scanner_result_path + File.separator;

        clearSpecificFilesInDirectory(filePath, Global.os_scanner_result_name);
    }

    public static void clearFile(int number) {
        for (int i = 1; i <= number; i++) {

            String filePath = Global.os_scanner_result_path
                    + i
                    + File.separator;

            clearSpecificFilesInDirectory(filePath, Global.os_scanner_result_name);

        }
    }

    public static void clearSpecificFilesInDirectory(String directoryPath, String fileNameToClear) {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            System.out.println("目录不存在: " + directoryPath);
            return;
        }

        if (!directory.isDirectory()) {
            System.out.println("不是一个目录: " + directoryPath);
            return;
        }

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals(fileNameToClear)) {
                    clearFileContent(file);
                }
            }
        }
    }


    public static void clearFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            System.out.println("目录不存在: " + directoryPath);
            return;
        }

        if (!directory.isDirectory()) {
            System.out.println("不是一个目录: " + directoryPath);
            return;
        }

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    clearFileContent(file);
                }
            }
        }
    }

    public static void clearFileContent(File file) {
        try (FileWriter writer = new FileWriter(file, false)) {
            // 写入空内容以清空文件
            writer.write("");
            System.out.println("文件已清空: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("无法清空文件: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
}
