package com.metoo.nrsm.core.config.ssh.demo;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectManager {

    //记录连接的客户端
    public static Map<String, Session> clients = new ConcurrentHashMap<>();

    private String sid = null;

    public static void main(String[] args) {
        connectOnly("metoo@domain", "Metoo@89745000", "192.168.5.191", "show run");// 单次连接操作
    }

    public static void connectOnly() {
        Connection conn = new Connection("192.168.5.205", 22);
        try {
            conn.connect();
            // 第一种,使用用户名密码验证身份
            boolean isAuthenticated = conn.authenticateWithPassword("nrsm", "metoo89745000");
            if (isAuthenticated == false) {
                throw new IOException("Authentication failed");
            }
            // 第二种,使用用户名密码以及秘钥验证身份
            //  conn.authenticateWithPassword(/*用户名*/userName, /*秘钥文件*/keyFile, /*密码*/password);
            // 此时就可以用conn do something啦!
            Session session = conn.openSession();

            clients.put(UUID.randomUUID().toString(), session);
            session.execCommand(/*要执行的命令*/"ifconfig");
// 获取输入流用来读取执行结果,自己封装流read即可
            InputStream is = new StreamGobbler(session.getStdout());

            int len = 0;//读取一个字节
            while ((len = is.read()) != -1) {
                System.out.print((char) len);  //abc
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void connectOnly(String username, String password, String host, String command) {
        Connection conn = new Connection(host);
        try {
            conn.connect();
            // 第一种,使用用户名密码验证身份
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (isAuthenticated == false) {
                throw new IOException("Authentication failed");
            }
            // 第二种,使用用户名密码以及秘钥验证身份
            //  conn.authenticateWithPassword(/*用户名*/userName, /*秘钥文件*/keyFile, /*密码*/password);
            // 此时就可以用conn do something啦!
            Session session = conn.openSession();

            clients.put(UUID.randomUUID().toString(), session);
            session.execCommand(/*要执行的命令*/command);
// 获取输入流用来读取执行结果,自己封装流read即可
            InputStream is = new StreamGobbler(session.getStdout());

            int len = 0;//读取一个字节
            while ((len = is.read()) != -1) {
                System.out.print((char) len);  //abc
            }
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 连续发送命令
    public static void continuous() {

    }


    @Scheduled(cron = "*/10 * * * * ?")
    public void ExecutionTimer2() {
        for (String key : clients.keySet()) {// 校验用户是否已断开，或断开时删除该用户定时任务信息
            try {
                Session session = clients.get(key);
                session.execCommand(/*要执行的命令*/"ifconfig");
// 获取输入流用来读取执行结果,自己封装流read即可
                InputStream is = new StreamGobbler(session.getStdout());

                int len = 0;//读取一个字节
                while ((len = is.read()) != -1) {
                    System.out.print((char) len);  //abc
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}
