package com.metoo.nrsm.core.utils.py.ssh;

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

//        session.execCommand("traceroute 202.103.100.247");
        session.execCommand("nohup python3 /opt/nrsm/py/dnsredis.py 0 &");

        consumeInputStream2(session.getStdout());
    }

    /**
     *   消费inputstream，并返回
     */
    public static String consumeInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s ;
        StringBuilder sb = new StringBuilder();
        while((s=br.readLine())!=null){
            sb.append(s);
        }
        return sb.toString();
    }

    public static void consumeInputStream2(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while((line = br.readLine())!=null){
            System.out.println(line);
        }
    }
}
