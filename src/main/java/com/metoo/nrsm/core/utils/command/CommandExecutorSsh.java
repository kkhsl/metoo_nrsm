package com.metoo.nrsm.core.utils.command;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.metoo.nrsm.core.utils.Global;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CommandExecutorSsh {


    public static void main(String[] args) {
        String restart = exec("sudo systemctl restart unbound");
        if("".contains(restart)){
            System.out.println("启动成功");
        }else{
            System.out.println("启动失败");
        }
        String status = exec("sudo systemctl status unbound");
        if(status.contains("Active: active (running)")){
            System.out.println("启动成功");
        }else{
            System.out.println("启动失败");
        }
    }

    public static String exec(String command) {
        Connection conn = null;
        Session session = null;

        try {
            // 创建连接
//            conn = new Connection(Global.host, Global.port);
//            // 启动连接
//            conn.connect();
//            // 验证用户密码
//            conn.authenticateWithPassword(Global.username, Global.password);

            conn = new Connection("192.168.6.101", 22);
            // 启动连接
            conn.connect();
            // 验证用户密码
            conn.authenticateWithPassword("root", "Metoo89745000!");

            // 重启 Unbound 服务
            session = conn.openSession();
            session.execCommand(command);

            String output = consumeInputStream(session.getStdout());
            System.out.println("Standard Output: " + output);
            String errorOutput = consumeInputStream(session.getStderr());
            System.out.println("Error Output: " + errorOutput);

            // 判断命令是否执行成功
            if (errorOutput.length() > 0) {
                // 有错误输出，说明执行失败
                return errorOutput.toString();
            } else {
                // 没有错误输出，说明命令执行成功
                return output.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        } finally {
            if(session != null){
                session.close(); // 关闭会话
            }
            if(conn != null){
                conn.close();
            }
        }
    }

    private static String consumeInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
