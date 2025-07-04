package com.metoo.nrsm.core.config.utils.gather.utils;


import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-28 10:19
 */
public class SshTracerouteDemo {

    @Test
    public void test() throws IOException {
        String host = "192.168.5.101";
        int port = 22;
        String username = "root";
        String password = "metoo89745000";
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);

        Session session = conn.openSession();

        session.execCommand("python3 /opt/sqlite/script/main.py h3c switch 192.168.100.1 ssh 22 metoo metoo89745000 aliveint");

        consumeInputStream2(session.getStdout());
    }

    @Test
    public void test205() throws IOException {
        String host = "192.168.5.205";
        int port = 22;
        String username = "nrsm";
        String password = "metoo89745000";
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);

        Session session = conn.openSession();

        session.execCommand("python3 /opt/nrsm/py/getarp.py 192.168.5.1 v2c public@123");

        consumeInputStream2(session.getStdout());
    }

    /**
     * 消费inputstream，并返回
     */
    public static String consumeInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static void consumeInputStream2(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }
}
