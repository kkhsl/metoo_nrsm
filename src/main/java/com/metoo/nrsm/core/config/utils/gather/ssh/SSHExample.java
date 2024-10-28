package com.metoo.nrsm.core.config.utils.gather.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-25 15:30
 */
public class SSHExample {

    public static void main(String[] args) {
        String host = "192.168.5.101";
        String username = "root";
        String password = "metoo89745000";
        String command1 = "cd /opt/sqlite/script && python3 /opt/sqlite/script/main.py h3c switch 192.168.100.1 ssh 22 metoo metoo89745000 aliveint";

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no"); // 忽略主机密钥检查
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            // 先进入指定目录
            channel.setCommand(command1);

            // 获取命令执行的输出
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            // 执行命令
            channel.connect();

            // 等待命令执行完成
            while (!channel.isClosed()) {
                Thread.sleep(1000);
            }

            // 输出命令执行结果
            java.io.InputStream in = channel.getInputStream();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }

            // 关闭连接
            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
