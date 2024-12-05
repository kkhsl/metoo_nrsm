package com.metoo.nrsm.core.config.utils.gather.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;

public class PythonExecutor {

    public static void main(String[] args) {
        String host = "192.168.5.205"; // 远程服务器的地址
        String user = "nrsm";          // SSH 登录用户名
        String password = "metoo89745000";      // SSH 登录密码
        String pythonScript = "/opt/nrsm/py/getarp.py";  // Python 脚本的完整路径
        String ipAddress = "192.168.100.3";
        String version = "v2c";
        String community = "public@123";

        // 构建命令行
        String command = String.format(
                "python %s %s %s %s",
                pythonScript, ipAddress, version, community);

        try {
            // 创建 JSch 会话
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            // 配置 SSH 会话
            session.setConfig("StrictHostKeyChecking", "no");  // 禁止主机验证
            session.setTimeout(30000);  // 设置连接超时

            // 连接到远程服务器
            session.connect();

            // 创建一个通道，用于执行命令
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);  // 设置要执行的命令

            // 获取输出流
            InputStream in = channel.getInputStream();
            channel.connect();  // 执行命令

            // 打印输出
            byte[] buffer = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(buffer, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("Exit status: " + channel.getExitStatus());
                    break;
                }
                Thread.sleep(1000);
            }

            // 关闭通道和会话
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
