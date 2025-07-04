package com.metoo.nrsm.core.network.networkconfig.test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class startDhcpd {

    /**
     * 控制 DHCP 服务状态 startdhcpd.py
     *
     * @param action 操作命令 (start/stop/restart等)
     * @return "True" 操作成功 | "False" 操作失败
     */
    public static String controlService(String action) {
        try {
            // 构建进程命令（参数安全分割）
            ProcessBuilder pb = new ProcessBuilder()
                    .command("systemctl", action, "isc-dhcp-server")
                    .redirectErrorStream(true); // 合并错误流到标准输出

            // 启动进程并等待完成
            Process process = pb.start();
            int exitCode = process.waitFor();

            // 读取命令输出（调试用）
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                StringBuilder output = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append("\n");
                }
                System.err.println("Command Output:\n" + output);
            }

            // 根据退出码返回结果
            return exitCode == 0 ? "True" : "False";
        } catch (IOException | InterruptedException e) {
            System.err.println("执行异常: " + e.getMessage());
            return "False";
        }
    }


    public static void main(String[] args) {
        String action = "status";  //status  stop  restart  start
        Session session = null;
        ChannelExec channel = null;
        try {
            session = SnmpHelper.createSession();
            // 创建一个执行频道
            channel = (ChannelExec) session.openChannel("exec");
            String command = "systemctl " + action + " isc-dhcp-server";
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