package com.metoo.nrsm.core.network.networkconfig.test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.*;

public class pingOp {

    /**
     * 执行 systemctl 命令  pingop.py
     *
     * @param action  status  stop  restart  start
     * @param service checkaliveip
     * @return
     */
    public static String executeSystemctl(String action, String service) {
        String command = "systemctl " + action + " " + service;
        Process process = null;
        try {
            // 使用安全的方式执行命令
            ProcessBuilder pb = new ProcessBuilder(splitCommand(command));
            pb.redirectErrorStream(true); // 合并错误流到输入流
            process = pb.start();

            // 必须消费输出流防止阻塞
            consumeOutputStream(process.getInputStream());

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "True";
            } else {
                return "False";
            }
        } catch (IOException | InterruptedException e) {
            return "False";
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 安全分割命令
     */
    private static String[] splitCommand(String command) {
        return new String[]{
                "/bin/sh",
                "-c",
                // 过滤特殊字符
                command.replaceAll("[^a-zA-Z0-9_\\-/. ]", "")
        };
    }

    /**
     * 异步消费输出流
     */
    private static void consumeOutputStream(InputStream inputStream) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream))) {
                while (reader.readLine() != null) {

                }
            } catch (IOException e) {

            }
        }).start();
    }

    // 主方法入口
    public static void main(String[] args) {
        String action = "start";  //status  stop  restart  start
        String service = "checkaliveip";  //checkaliveip
        Session session = null;
        ChannelExec channel = null;
        try {
            session = SnmpHelper.createSession();
            // 创建一个执行频道
            channel = (ChannelExec) session.openChannel("exec");
            String command = "systemctl " + action + " " + service;
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            // 获取命令输出
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
            channel.connect();

            while (!channel.isClosed()) {
                Thread.sleep(500);
            }
            // 6. 处理执行结果
            int exitStatus = channel.getExitStatus();
            String output = outputBuffer.toString();
            if (exitStatus == 0) {
                System.out.println("True");
            } else {
                System.out.println("False");
            }

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