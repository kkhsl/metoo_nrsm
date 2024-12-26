package com.metoo.nrsm.core.utils.py.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class JSchSSHConnectionExample {

    public static void main(String[] args) {
        String hostname = "192.168.60.90";  // 服务器 IP
        String username = "zzf";  // SSH 用户名
        String password = "Transfar@123";  // SSH 密码

        JSch jsch = new JSch();
        Session session = null;

        try {
            // 创建 SSH 会话
            session = jsch.getSession(username, hostname, 22);  // 默认端口 22
            session.setPassword(password);

//            session.setConfig("kex", "diffie-hellman-group14-sha256,ecdh-sha2-nistp256,curve25519-sha256@libssh.org");
//            session.setConfig("cipher", "aes256-ctr,aes128-ctr");
//            session.setConfig("mac", "hmac-sha2-256,hmac-sha2-512");

            jsch.setKnownHosts("C:\\Users\\hkk\\.ssh\\known_hosts");

            // 关闭严格检查（适用于首次连接，防止 host key 问题）
//            session.setConfig("StrictHostKeyChecking", "no"); // 允许 JSch 跳过主机密钥验证

            // 连接到远程服务器
            session.connect();

            // 执行命令（例如 `ls`）
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("ls -al");

            // 获取命令输出
            channel.setInputStream(null);
            channel.setOutputStream(System.out);
            channel.connect();

            // 等待命令执行完成
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            System.out.println("Exit code: " + channel.getExitStatus());

            // 关闭连接
            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
