package com.metoo.nrsm.core.utils.unbound.test;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class UnboundConfForward {

    private static final String configFilePath = "C:\\Users\\46075\\Desktop\\新建文件夹 (2)\\unbound.conf";

    // 更新 forward-zone 配置，删除不需要的地址，添加缺失的地址
    public static void updateForwardZoneConfig(String filePath, Set<String> validForwardAddresses) throws IOException {
        // 读取配置文件内容
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<String> updatedLines = new ArrayList<>();
        boolean forwardZoneSectionFound = false;
        boolean skipForwardZone = false;

        // 存储当前文件中所有的 forward-addr
        Set<String> existingAddresses = new HashSet<>();

        // 遍历文件的每一行
        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith("forward-zone:") && validForwardAddresses.size() <= 0) {
                skipForwardZone = true; // 设置跳过标志，后续的行会被跳过
                continue; // 跳过当前 forward-zone 配置块
            }

            if (skipForwardZone) {
                skipForwardZone = false;
                continue;
            }
            if (trimmedLine.startsWith("forward-zone:")) {
                forwardZoneSectionFound = true;
                updatedLines.add(line); // 保留 forward-zone 配置块的起始部分
            } else if (forwardZoneSectionFound && trimmedLine.startsWith("forward-addr:")) {
                String address = trimmedLine.split(" ")[1].trim();
                existingAddresses.add(address); // 将文件中存在的 forward-addr 地址加入集合
                updatedLines.add(line); // 保留现有的 forward-addr
            } else {
                updatedLines.add(line); // 保留其他未修改的部分
            }
        }

        // 如果没有找到 forward-zone 配置块，添加它
        if (validForwardAddresses.size() > 0) {
            if (!forwardZoneSectionFound) {
                updatedLines.add("\nforward-zone:");
                updatedLines.add("  name: \".\"");
            }
        }
        // 删除不需要的 forward-addr 地址
        ListIterator<String> iterator = updatedLines.listIterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.trim().startsWith("forward-addr:")) {
                String address = line.trim().split(" ")[1].trim();
                if (!validForwardAddresses.contains(address)) {
                    iterator.remove(); // 如果地址不在 validForwardAddresses 中，删除该行
                }
            }
        }

        // 添加缺失的 forward-addr 地址
        for (String address : validForwardAddresses) {
            if (!existingAddresses.contains(address)) {
                updatedLines.add("  forward-addr: " + address);
            }
        }

        // 写回更新后的内容到配置文件
        Files.write(Paths.get(filePath), updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void main(String[] args) throws IOException {
        // 模拟 validForwardAddresses (可以从外部获取)
        Set<String> validForwardAddresses = new HashSet<>(Arrays.asList("223.5.5.5", "119.29.29.29", "2400:3200::1"));

        // 更新配置文件
        updateForwardZoneConfig(configFilePath, validForwardAddresses);
    }


    public static List<String> updateConfig(List<String> lines, Set<String> validForwardAddresses) throws IOException {
        // 读取配置文件内容
        List<String> updatedLines = new ArrayList<>();
        boolean forwardZoneSectionFound = false;
        boolean skipForwardZone = false;

        // 存储当前文件中所有的 forward-addr
        Set<String> existingAddresses = new HashSet<>();

        // 遍历文件的每一行
        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith("forward-zone:") && validForwardAddresses.size() <= 0) {
                skipForwardZone = true; // 设置跳过标志，后续的行会被跳过
                continue; // 跳过当前 forward-zone 配置块
            }

            if (skipForwardZone) {
                skipForwardZone = false;
                continue;
            }
            if (trimmedLine.startsWith("forward-zone:")) {
                forwardZoneSectionFound = true;
                updatedLines.add(line); // 保留 forward-zone 配置块的起始部分
            } else if (forwardZoneSectionFound && trimmedLine.startsWith("forward-addr:")) {
                String address = trimmedLine.split(" ")[1].trim();
                existingAddresses.add(address); // 将文件中存在的 forward-addr 地址加入集合
                updatedLines.add(line); // 保留现有的 forward-addr
            } else {
                updatedLines.add(line); // 保留其他未修改的部分
            }
        }

        // 如果没有找到 forward-zone 配置块，添加它
        if (validForwardAddresses.size() > 0) {
            if (!forwardZoneSectionFound) {
                updatedLines.add("\nforward-zone:");
                updatedLines.add("  name: \".\"");
            }
        }
        // 删除不需要的 forward-addr 地址
        ListIterator<String> iterator = updatedLines.listIterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.trim().startsWith("forward-addr:")) {
                String address = line.trim().split(" ")[1].trim();
                if (!validForwardAddresses.contains(address)) {
                    iterator.remove(); // 如果地址不在 validForwardAddresses 中，删除该行
                }
            }
        }

        // 添加缺失的 forward-addr 地址
        for (String address : validForwardAddresses) {
            if (!existingAddresses.contains(address)) {
                updatedLines.add("  forward-addr: " + address);
            }
        }

        // 写回更新后的内容到配置文件
        return updatedLines;
    }
}
