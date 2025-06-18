package com.metoo.nrsm.core.network.mac;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;

public class MacAddressFetcher {

    // 获取远程Linux主机的MAC地址
    public String getMacAddress(String host, String username, String password) {
        String macAddress = null;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, 22);
            session.setPassword(password);

            // 避免第一次连接时提示确认主机密钥
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 使用 ifconfig 命令获取 MAC 地址
            String command = "ifconfig -a | grep -Po '([0-9a-f]{2}:){5}[0-9a-f]{2}'";

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream inputStream = channel.getInputStream();

            channel.connect();

            // 读取命令输出
            byte[] buffer = new byte[1024];
            while (true) {
                while (inputStream.available() > 0) {
                    int bytesRead = inputStream.read(buffer, 0, 1024);
                    if (bytesRead < 0) break;
                    macAddress = new String(buffer, 0, bytesRead);
                }
                if (channel.isClosed()) break;
                try { Thread.sleep(1000); } catch (Exception ee) {}
            }
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return macAddress != null ? macAddress.trim() : null;
    }

    public static void main(String[] args) {
        MacAddressFetcher fetcher = new MacAddressFetcher();


        String mac = fetcher.getMacAddress("192.168.6.101", "root", "Metoo89745000!");
        System.out.println("MAC Address: " + mac);
    }
}
