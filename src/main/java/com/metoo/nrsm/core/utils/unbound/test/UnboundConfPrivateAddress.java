package com.metoo.nrsm.core.utils.unbound.test;

import com.github.pagehelper.util.StringUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnboundConfPrivateAddress {

    // 根据 privateAddress 的值注释或取消注释配置文件中的 private-address 行
    public static List<String> updateConfig(List<String> lines, boolean privateAddress) throws IOException {

        List<String> updatedLines = new ArrayList<>();

        // 遍历每一行
        for (String line : lines) {
            // 去掉行首尾的空格并去除注释符号（如果有）
            String trimmedLine = line.trim();
            String indentation = line.substring(0, line.indexOf(trimmedLine));
            System.out.println(trimmedLine.replaceFirst("^#\\s*", ""));

            // 判断该行是否包含 "private-address: ::/0"（无论是否有注释）
            if (trimmedLine.replaceFirst("^#\\s*", "").equals("private-address: ::/0")) {
                // 获取当前行的缩进部分
                if (privateAddress) {
                    // 如果 privateAddress 为 true 且该行是注释掉的，则去掉注释
                    updatedLines.add(trimmedLine.startsWith("#") ? indentation + trimmedLine.substring(1).trim() : indentation + trimmedLine);
                } else {
                    // 如果 privateAddress 为 false，则注释掉该行
                    updatedLines.add(trimmedLine.startsWith("#") ? indentation + trimmedLine : indentation + "# " + trimmedLine);
                }
            } else {
                // 对于其他行，直接添加
                updatedLines.add(line);
            }
        }
        return updatedLines;
    }


    // 根据 localZone 配置删除不必要的 local-zone 和 local-data 行
    public static void deleteLocalZone(String filePath, Set<String> validZoneNames) throws IOException {
        validZoneNames.add("bbb.com.");
        validZoneNames.add("aaa.com.");
        // 读取配置文件内容
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        // 打开输出流
        List<String> updatedLines = new ArrayList<>();
        boolean skipLocalData = false; // 标记是否跳过 local-data 行

        for (String line : lines) {
            // 获取当前行的缩进部分
            String trimmedLine = line.trim();
            String indentation = line.substring(0, line.indexOf(trimmedLine));

            // 处理 local-zone 行
            if (trimmedLine.replaceFirst("^#\\s*", "").startsWith("local-zone:")
                    && trimmedLine.contains("\"")) {
                String zoneName = trimmedLine.split(":")[1].trim().split(" ")[0].trim().replace("\"", "");
                // 如果该 zoneName 不在 validZoneNames 中，则跳过这行及对应的 local-data 行
                if (!validZoneNames.isEmpty()) {
                    if (!validZoneNames.contains(zoneName)) {
                        skipLocalData = true;
                        continue; // 删除该 local-zone 行
                    } else {
                        skipLocalData = false;
                    }
                }
            }

            // 处理 local-data 行
            if (skipLocalData && trimmedLine.replaceFirst("^#\\s*", "").startsWith("local-data:")) {
                continue; // 删除对应的 local-data 行
            }

            // 将未删除的行添加到 updatedLines
            updatedLines.add(line);
        }

        // 将更新后的内容写回配置文件
        Files.write(Paths.get(filePath), updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
