package com.metoo.nrsm.core.network.networkconfig.test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class getDns {

    /**
     * getdns.py
     * @return  ["223.5.5.5"]
     */
    public static String getDnsSettings() {
        List<String> dnsList = new ArrayList<>();
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader("/etc/resolv.conf"))) {
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("nameserver")) {
                    String nameserver = line.split("\\s+")[1].trim();
                    dnsList.add(nameserver);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dnsList.stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }


    public static void main(String[] args) {
        Session session = null;
        ChannelExec channel = null;
        List<String> dnsList = new ArrayList<>();
        try {
            session= SnmpHelper.createSession();
            // 创建一个执行频道
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("cat /etc/resolv.conf"); // 获取 DNS 设置
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            // 获取命令输出
            InputStream in = channel.getInputStream();
            channel.connect();
            // 读取输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("nameserver")) {
                        dnsList.add(line.split("\\s+")[1]);
                    }
                }
            }
            // 转换为JSON数组格式
            String jsonOutput = dnsList.stream()
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(", ", "[", "]"));
            System.out.println(jsonOutput);
        } catch (Exception e) {
            System.err.println("Error during remote connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭频道和会话
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }


} 