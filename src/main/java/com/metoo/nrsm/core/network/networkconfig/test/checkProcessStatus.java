package com.metoo.nrsm.core.network.networkconfig.test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class checkProcessStatus {
    /**
     * checkdhcpd.py  checkdns.py  checkping.py  checkprocess.py
     * @param process   "dhcpd", "dhcpd6", "checkaliveip", "Dnsredis"
     * @return
     */
    public static String checkProcessStatus(String process) {
        String command = "";
        switch (process) {
            case "dhcpd":
                command = "ps -ef | grep dhcpd.conf | grep -v grep";
                break;
            case "dhcpd6":
                command = "ps -ef | grep dhcpd6.conf | grep -v grep";
                break;
            case "checkaliveip":
                command = "ps -ef | grep checkaliveip.py | grep -v grep";
                break;
            case "Dnsredis":
                command = "ps -ef | grep Dnsredis.py | grep -v grep";
                break;
            default:
                return null;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process proc = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            if (!output.toString().isEmpty()){
                return "True";
            }else {
                return "None";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "None";
        }
    }

    public static void main(String[] args) {
        // 测试 {"dhcpd", "dhcpd6", "checkaliveip", "Dnsredis"}
        Session session = null;
        ChannelExec channel = null;
        String command = "";
        String process="dhcpd6";     //{"dhcpd", "dhcpd6", "checkaliveip", "Dnsredis"}
        try {
            session= SnmpHelper.createSession();
            // 创建一个执行频道
            channel = (ChannelExec) session.openChannel("exec");
            switch (process) {
                case "dhcpd":
                    command = "ps -ef | grep dhcpd.conf | grep -v grep";
                    break;
                case "dhcpd6":
                    command = "ps -ef | grep dhcpd6.conf | grep -v grep";
                    break;
                case "checkaliveip":
                    command = "ps -ef | grep checkaliveip.py | grep -v grep";
                    break;
                case "Dnsredis":
                    command = "ps -ef | grep Dnsredis.py | grep -v grep";
                    break;
            }


            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            // 获取命令输出
            InputStream in = channel.getInputStream();
            channel.connect();

            // 读取输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                if (!output.toString().isEmpty()){
                    System.out.println("True");
                }else {
                    System.out.println("None");
                }
            }
        } catch (Exception e) {
            System.err.println("Error during remote connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭频道和会话
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
