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
        String host = "175.6.37.154";
        int port = 22;
        String username = "root";
        String password = "Metoo89745000!";
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);

        Session session = conn.openSession();

//        session.execCommand("traceroute 202.103.100.247");
//        session.execCommand("nohup python3 /opt/nrsm/py/Dnsredis.py 0 &");

//        python3 gettraffic.py --type h3c --vendor h3c --command get_ipv4_port --dhost 202.103.100.254 --version v2c --community transfar@123 --ip 172.16.253.253 --oid '1.3.6.1.2.1.4.20.1.2'
        session.execCommand("python3 gettraffic.py --type h3c --vendor h3c --command get_ipv4_port --dhost 202.103.100.254 --version v2c --community transfar@123 --ip 172.16.253.253 --oid '1.3.6.1.2.1.4.20.1.2'");

        consumeInputStream2(session.getStdout());
    }


    @Test
    public void test2() throws IOException {
        String host = "175.6.37.154";
        int port = 22;
        String username = "root";
        String password = "Metoo89745000!";
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);

        Session session = conn.openSession();

//        session.execCommand("traceroute 202.103.100.247");
//        session.execCommand("nohup python3 /opt/nrsm/py/Dnsredis.py 0 &");

//        python3 gettraffic.py --type h3c --vendor h3c --command get_ipv4_port --dhost 202.103.100.254 --version v2c --community transfar@123 --ip 172.16.253.253 --oid '1.3.6.1.2.1.4.20.1.2'
        session.execCommand("python3 --version");

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
