package com.metoo.nrsm.core.network.networkconfig.test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.*;


public class dhcpDop {
    /**
     * dhcpdop.py
     *
     * @param operation "restart"  "stop"
     * @param service   "dhcpd6"   "dhcpd"
     * @return
     */
    public static String processOp(String operation, String service) {
        if ("dhcpd".equals(service)) {
            service = "isc-dhcp-server";
        } else if ("dhcpd6".equals(service)) {
            service = "isc-dhcp-server6";
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        String command = String.format("systemctl %s %s", operation, service);
        processBuilder.command("bash", "-c", command);

        try {
            Process process = processBuilder.start();

            // 异步读取输出流
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Thread outputThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = outputReader.readLine()) != null) {
                        System.out.println("[OUT] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 异步读取错误流
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            Thread errorThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("[ERR] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            outputThread.start();
            errorThread.start();

            int exitCode = process.waitFor();
            outputThread.join();
            errorThread.join();

            // 返回字符串结果
            return exitCode == 0 ? "True" : "False";

        } catch (IOException | InterruptedException e) {
            return "False";
        }
    }


    public static void main(String[] args) {
        Session session = null;
        ChannelExec channel = null;
        String operation = "restart";    //stop
        String service = "dhcpd6";       //dhcpd6
        try {
            session = SnmpHelper.createSession();
            // 创建一个执行频道
            channel = (ChannelExec) session.openChannel("exec");
            if ("dhcpd".equals(service)) {
                service = "isc-dhcp-server";
            } else if ("dhcpd6".equals(service)) {
                service = "isc-dhcp-server6";
            }
            String command = String.format("systemctl %s %s", operation, service);
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            // 捕获错误流
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            channel.setErrStream(errorStream);

            try (InputStream in = channel.getInputStream()) {
                channel.connect();

                // 等待执行完成
                while (!channel.isClosed()) {
                    Thread.sleep(500);
                }

                // 验证退出码
                if (channel.getExitStatus() != 0) {
                    System.out.println("False");
                    throw new IOException("Command failed: " + errorStream.toString());
                } else {
                    System.out.println("True");
                }
            }
        } catch (Exception e) {
            System.out.println("False");
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