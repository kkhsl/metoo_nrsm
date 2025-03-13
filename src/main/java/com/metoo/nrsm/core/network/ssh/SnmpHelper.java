package com.metoo.nrsm.core.network.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SnmpHelper {
    private static final String username = "root";
    private static final String host = "192.168.6.102";
    private static final int port = 22;
    private static final String password = "Metoo89745000!";

    // 提取创建 SSH 会话的逻辑
    public static Session createSession() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }
}
