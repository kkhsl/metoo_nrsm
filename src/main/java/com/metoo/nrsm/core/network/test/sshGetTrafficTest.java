package com.metoo.nrsm.core.network.test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.jopo.SnmpWalkResult;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class sshGetTrafficTest {

    public String test(SnmpWalkResult snmpWalkResult, int vlanId) {
        StringBuilder result = new StringBuilder();
        try {
            Session session = SnmpHelper.createSession();
            String command = String.format("dis int vlanif %d", vlanId);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            // 获取命令输出
            channel.connect();

            InputStream in = channel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public void parseTrafficData(String output) {
        String ipv4InputRate = "0";
        String ipv4OutputRate = "0";
        String ipv6InputRate = "0";
        String ipv6OutputRate = "0";

        String[] lines = output.split("\n");
        boolean isIPv4Section = false;
        boolean isIPv6Section = false;

        for (String line : lines) {
            if (line.contains("Ipv4:")) {
                isIPv4Section = true;
                isIPv6Section = false;
            } else if (line.contains("Ipv6:")) {
                isIPv6Section = true;
                isIPv4Section = false;
            }

            if (isIPv4Section || isIPv6Section) {
                // 提取输入速率
                if (line.contains("Last 300 seconds input rate")) {
                    String[] parts = line.split(" ");
                    String inputRate = parts[parts.length - 4]; // 获取输入速率
                    if (isIPv4Section) {
                        ipv4InputRate = inputRate;
                    } else if (isIPv6Section) {
                        ipv6InputRate = inputRate;
                    }
                }

                // 提取输出速率（移除了处理"Output:"行的代码）
                if (line.contains("Last 300 seconds output rate")) {
                    String[] parts = line.split(" ");
                    String outputRate = parts[parts.length - 4]; // 获取输出速率
                    if (isIPv4Section) {
                        ipv4OutputRate = outputRate;
                    } else if (isIPv6Section) {
                        ipv6OutputRate = outputRate;
                    }
                }
            }
        }

        System.out.println("IPv4 Input Rate: " + ipv4InputRate);
        System.out.println("IPv4 Output Rate: " + ipv4OutputRate);
        System.out.println("IPv6 Input Rate: " + ipv6InputRate);
        System.out.println("IPv6 Output Rate: " + ipv6OutputRate);
    }

    public static void main(String[] args) {
        sshGetTrafficTest client = new sshGetTrafficTest();

        // 构造数据以模拟从核心交换机获取的输出
        String output =
                "Vlanif10 current state : UP (ifindex: 174)\n" +
                        "Last 300 seconds input rate 942712 bits/sec, 362 packets/sec\n" +
                        "Last 300 seconds output rate 4422712 bits/sec, 1205 packets/sec\n" +
                        "Ipv4:\n" +
                        "Last 300 seconds input rate 942712 bits/sec, 362 packets/sec\n" +
                        "Last 300 seconds output rate 4422712 bits/sec, 1205 packets/sec\n" +
                        "Input:  111581 packets, 36400556 bytes\n" +
                        "Output:  373773 packets, 171628699 bytes\n" +
                        "Ipv6:\n" +
                        "Last 300 seconds input rate 0 bits/sec, 0 packets/sec\n" +
                        "Last 300 seconds output rate 0 bits/sec, 0 packets/sec\n" +
                        "Input:  0 packets, 0 bytes\n" +
                        "Output:  0 packets, 0 bytes\n";

        // 调用解析方法
        client.parseTrafficData(output);
    }

}