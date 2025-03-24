package com.metoo.nrsm.core.network.networkconfig.test;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class modifyDhcp {

    // 配置文件路径
    private static final String DHCP_CONFIG_PATH = "/etc/default/isc-dhcp-server";

    /**
     * 保存 DHCP 接口配置   modifydhcp.py
     * @param v4status 是否启用IPv4服务 ("true"/"false")
     * @param v4int     IPv4接口名称
     * @param v6status  是否启用IPv6服务 ("true"/"false")
     * @param v6int     IPv6接口名称
     */
    public static void dhcpsave(String v4status, String v4int, 
                               String v6status, String v6int) throws IOException {
        // 1. 读取当前配置状态
        Map<String, String> current = parseCurrentConfig();
        
        // 2. 处理 IPv4 配置变更
        processInterface(current, 
                         "v4", v4status, v4int, 
                         current.get("v4status"), current.get("v4int"));
        
        // 3. 处理 IPv6 配置变更
        processInterface(current,
                         "v6", v6status, v6int,
                         current.get("v6status"), current.get("v6int"));
    }

    /**
     * 解析当前配置文件状态
     */
    private static Map<String, String> parseCurrentConfig() throws IOException {
        Map<String, String> status = new HashMap<>();
        status.put("v4status", "false");
        status.put("v4int", "");
        status.put("v6status", "false");
        status.put("v6int", "");

        for (String line : Files.readAllLines(Paths.get(DHCP_CONFIG_PATH))) {
            line = line.trim();
            checkLine(line, "v4", status);
            checkLine(line, "v6", status);
        }
        return status;
    }

    private static void checkLine(String line, String version, Map<String, String> status) {
        String prefix = "INTERFACES" + version;
        String statusKey = version + "status";
        String intKey = version + "int";

        if (line.startsWith("#" + prefix + "=")) {
            status.put(statusKey, "false");
            extractInterface(line, intKey, status);
        } else if (line.startsWith(prefix + "=")) {
            status.put(statusKey, "true");
            extractInterface(line, intKey, status);
        }
    }

    private static void extractInterface(String line, String key, Map<String, String> status) {
        int start = line.indexOf('"');
        int end = line.lastIndexOf('"');
        if (start != -1 && end != -1 && start < end) {
            status.put(key, line.substring(start + 1, end));
        }
    }

    /**
     * 处理单个协议版本配置变更
     */
    private static void processInterface(Map<String, String> current,
                                        String version,
                                        String newStatus, String newInt,
                                        String oldStatus, String oldInt) throws IOException {
        String prefix = "INTERFACES" + version;
        
        if ("true".equals(newStatus)) {
            if ("true".equals(oldStatus)) {
                if (!newInt.equals(oldInt)) {
                    updateConfigLine(prefix, newInt, false); // 更新接口
                }
            } else {
                uncommentAndSet(prefix, newInt); // 取消注释并设置
            }
        } else {
            if ("true".equals(oldStatus)) {
                commentLine(prefix); // 注释生效行
            }
        }
    }

    /**
     * 更新指定配置行（支持注释状态）
     */
    private static void updateConfigLine(String prefix, String newInt, boolean comment) 
            throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(DHCP_CONFIG_PATH));
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().startsWith(prefix + "=") || 
                line.trim().startsWith("#" + prefix + "=")) {
                String newLine = prefix + "=\"" + newInt + "\"";
                newLines.add(comment ? "#" + newLine : newLine);
            } else {
                newLines.add(line);
            }
        }
        
        writeConfig(newLines);
    }

    /**
     * 取消注释并设置接口
     */
    private static void uncommentAndSet(String prefix, String newInt) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(DHCP_CONFIG_PATH));
        List<String> newLines = new ArrayList<>();
        boolean modified = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#" + prefix + "=")) {
                newLines.add(prefix + "=\"" + newInt + "\"");
                modified = true;
            } else if (trimmed.startsWith(prefix + "=")) {
                newLines.add(prefix + "=\"" + newInt + "\"");
                modified = true;
            } else {
                newLines.add(line);
            }
        }

        if (!modified) {
            newLines.add(prefix + "=\"" + newInt + "\"");
        }
        
        writeConfig(newLines);
    }

    /**
     * 注释指定配置行
     */
    private static void commentLine(String prefix) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(DHCP_CONFIG_PATH));
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().startsWith(prefix + "=")) {
                newLines.add("#" + line);
            } else {
                newLines.add(line);
            }
        }
        
        writeConfig(newLines);
    }

    /**
     * 安全写入配置文件
     */
    private static void writeConfig(List<String> lines) throws IOException {
        Path temp = Files.createTempFile("isc-dhcp-server", ".tmp");
        Files.write(temp, lines);
        Files.move(temp, Paths.get(DHCP_CONFIG_PATH), 
                  StandardCopyOption.REPLACE_EXISTING,
                  StandardCopyOption.ATOMIC_MOVE);
    }

    public static void main(String[] args) {
        try {
            // 示例：启用 IPv4 接口 eth0，禁用 IPv6
            dhcpsave("true", "eth0", "false", "");
        } catch (IOException e) {
            System.err.println("配置保存失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


}