package com.metoo.nrsm.core.network.networkconfig.test;

import com.google.gson.Gson;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;
import com.metoo.nrsm.core.utils.Global;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class getDhcp {


    /**
     * getdhcp.py
     * @return  {"v6int":"","v4int":"","v6status":"true","v4status":"true"}
     */
    public static String getDhcpStatus() {
        Map<String, String> status = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("/etc/default/isc-dhcp-server"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 处理IPv4配置
                if (line.startsWith("#INTERFACESv4=")) {
                    status.put("v4status", "false");
                    status.put("v4int", "");
                } else if (line.startsWith("INTERFACESv4=")) {
                    parseInterfaceLine(line, "v4", status);
                }
                // 处理IPv6配置
                if (line.startsWith("#INTERFACESv6=")) {
                    status.put("v6status", "false");
                    status.put("v6int", "");
                } else if (line.startsWith("INTERFACESv6=")) {
                    parseInterfaceLine(line, "v6", status);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Gson().toJson(status);
    }

    /**
     * 解析接口配置行，提取状态和接口名
     * @param line    配置行（如 INTERFACESv4="eth0"）
     * @param version 协议版本标识（"v4" 或 "v6"）
     * @param status  存储结果的Map
     */
    private static void parseInterfaceLine(String line, String version, Map<String, String> status) {
        String[] parts = line.split("=", 2); // 分割键值对
        if (parts.length < 2) {
            status.put(version + "status", "true");
            status.put(version + "int", "");
            return;
        }
        String value = parts[1].trim();
        // 处理带引号的值
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).trim();
        }
        status.put(version + "status", "true");
        status.put(version + "int", value);
    }

    public static void main(String[] args) {
        Session session = null;
        ChannelExec channel = null;
        Map<String, String> status = new HashMap<>();
        try {
            session = SnmpHelper.createSession();
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("cat /etc/default/isc-dhcp-server");
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // 处理IPv4配置
                    if (line.startsWith("#INTERFACESv4=")) {
                        status.put("v4status", "false");
                        status.put("v4int", "");
                    } else if (line.startsWith("INTERFACESv4=")) {
                        parseInterfaceLine(line, "v4", status);
                    }
                    // 处理IPv6配置
                    if (line.startsWith("#INTERFACESv6=")) {
                        status.put("v6status", "false");
                        status.put("v6int", "");
                    } else if (line.startsWith("INTERFACESv6=")) {
                        parseInterfaceLine(line, "v6", status);
                    }
                }
                System.out.println(new Gson().toJson(status));
            }
        } catch (Exception e) {
            System.err.println("Error during remote connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
} 