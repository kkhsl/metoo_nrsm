package com.metoo.nrsm.core.utils.command;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileReadExecutorSsh {


    /**
     * @param path 文件绝对路径
     * @return
     */
    public static List<String> exe(String path) {
        String host = "192.168.6.101"; // 远程主机 IP
        int port = 22; // SSH 端口
        String user = "root"; // 用户名
        String password = "Metoo89745000!"; // 密码
        String filePath = path; // 配置文件路径

        try {
            // 初始化 JSch
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);

            // 设置密码
            session.setPassword(password);

            // 忽略主机验证
            session.setConfig("StrictHostKeyChecking", "no");

            // 建立连接
            System.out.println("Connecting to " + host + "...");
            session.connect();
            System.out.println("Connected to " + host);

            // 执行命令以读取文件内容
            String command = "cat " + filePath;
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);

            // 获取命令的输入流
            InputStream in = channelExec.getInputStream();

            // 打开通道
            channelExec.connect();

            // 读取文件内容
            // 获取进程的输出流
            List<String> lines = new ArrayList<>();
            try (Scanner scanner = new Scanner(in)) {
                System.out.println("File Content:");
                while (scanner.hasNextLine()) {
                    lines.add(scanner.nextLine());
                }
            }

            // 关闭通道和会话
            channelExec.disconnect();
            session.disconnect();

            System.out.println("Disconnected from " + host);
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
