package com.metoo.nrsm.core.network.hostname;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.jopo.SnmpWalkResult;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HostNameFetcher {

    // 通过SSH连接到192.168.6.101并读取192.168.6.1的主机名
//    public String getHostNameViaSSH(SnmpWalkResult snmpWalkResult) {
//        String hostName = null;
//        try {
//            Session session = SnmpHelper.createSession();
//            session.connect();
//
//            // 执行命令来获取目标主机的主机名
//             String command = String.format("snmpwalk -%s -c %s %s 1.3.6.1.2.1.1.5.0",
//                     snmpWalkResult.getVersion(), snmpWalkResult.getCommunity(), snmpWalkResult.getIp());
//
//            ChannelExec channel = (ChannelExec) session.openChannel("exec");
//            channel.setCommand(command);
//            channel.setInputStream(null);
//            channel.setErrStream(System.err);
//
//            // 获取命令输出
//            channel.connect();
//            InputStream in = channel.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (line.contains("STRING")) {
//                    hostName = line.split("STRING:")[1].trim();
//                }
//            }
//
//            channel.disconnect();
//            session.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return hostName;
//    }


    // 通过SSH连接到指定主机并读取目标主机的主机名
    public SnmpWalkResult getHostNameViaSSH(SnmpWalkResult snmpWalkResult) {
        String hostName = null;
        String errorMessage = null;
        Session session = null;
        BufferedReader reader = null;

        try {
            session = SnmpHelper.createSession(); // 创建SSH连接
            reader = executeSnmpWalkCommand(session, snmpWalkResult); // 执行snmpwalk命令并获取结果流

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("STRING")) {
                    hostName = line.split("STRING:")[1].trim();
                }
            }

        } catch (Exception e) {
            errorMessage = "Error fetching host name: " + e.getMessage();
            e.printStackTrace();
        } finally {
            // 确保资源在使用完后被关闭
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        // 将获取的结果封装到 SnmpWalkResult 对象中
        snmpWalkResult.setResult(hostName != null ? hostName : errorMessage);
        return snmpWalkResult;
    }

    // 执行 snmpwalk 命令并返回输出流
    private BufferedReader executeSnmpWalkCommand(Session session, SnmpWalkResult snmpWalkResult) throws JSchException, IOException {
        String command = String.format("snmpwalk -%s -c %s %s 1.3.6.1.2.1.1.5.0",
                snmpWalkResult.getVersion(), snmpWalkResult.getCommunity(), snmpWalkResult.getIp());

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);

        channel.connect();
        InputStream in = channel.getInputStream();
        return new BufferedReader(new InputStreamReader(in));
    }


    // 执行本地 snmpwalk 命令
    public static String executeSnmpWalk(SnmpWalkResult snmpWalkResult) {
        String result = "";
        try {
            // 构建 snmpwalk 命令
            String command = String.format("snmpwalk -%s -c%s %s 1.3.6.1.2.1.1.5.0",
                    snmpWalkResult.getVersion(), snmpWalkResult.getCommunity(), snmpWalkResult.getIp());

            // 使用 ProcessBuilder 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);  // 将错误流与输出流合并
            Process process = processBuilder.start();

            // 获取命令的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 获取命令执行的结果
            result = output.toString();
            process.waitFor(); // 等待命令执行完成
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        HostNameFetcher client = new HostNameFetcher();

        SnmpWalkResult snmpWalkResult = new SnmpWalkResult()
                .setIp("192.168.6.1")
                .setVersion("v2c")
                .setCommunity("public@123");

        SnmpWalkResult result = client.getHostNameViaSSH(snmpWalkResult);

        System.out.println("Host name of " + snmpWalkResult.getIp() + ": " + result.getResult());
    }

}
