package com.metoo.nrsm.core.config.ssh.demo;

import org.apache.commons.net.telnet.TelnetClient;
import java.io.InputStream;
import java.io.PrintStream;

public class TelnetDemo {
    public static void main(String[] args) {
        TelnetClient telnet = new TelnetClient();
        try {
            // 1. 建立连接
            System.out.println("正在连接 192.168.204.1:23...");
            telnet.connect("192.168.204.1", 23);

            // 2. 获取输入输出流
            InputStream in = telnet.getInputStream();
            PrintStream out = new PrintStream(telnet.getOutputStream());

            // 3. 读取欢迎信息
            System.out.println("连接成功，服务器响应:");
            readResponse(in);

            // 4. 发送测试命令 (根据你的设备调整)
            out.println("");  // 发送空行获取提示符
            out.flush();
            readResponse(in);

            // 5. 这里可以添加更多交互...

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (telnet.isConnected()) {
                    telnet.disconnect();
                    System.out.println("连接已关闭");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 读取服务器响应
    private static void readResponse(InputStream in) throws Exception {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            System.out.print(new String(buffer, 0, len));
            if (in.available() == 0) break; // 没有更多数据时退出
        }
    }
}