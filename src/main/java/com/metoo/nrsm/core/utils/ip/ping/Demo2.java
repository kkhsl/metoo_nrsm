package com.metoo.nrsm.core.utils.ip.ping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-26 14:56
 */
public class Demo2 {

    public static void main(String[] args) {
        String host = "192.168.5.101"; // 替换为你想要ping的主机
        String param = "ipscanner 192.168.5.101 -t";
        ping(param);
    }

    public static void ping(String param){
//        String host = "192.168.5.101"; // 替换为你想要ping的主机
//        ProcessBuilder builder = new ProcessBuilder("ipscanner", host, "-t"); // 修改参数以符合你的需求

        ProcessBuilder builder = new ProcessBuilder("ipscanner", "192.168.5.101", "-t"); // 修改参数以符合你的需求

        try {
            Process process = builder.start();

            // 读取ping命令的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK") ));

            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待ping命令执行完成
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
