package com.metoo.nrsm.core.network.test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.hostname.HostNameFetcher;
import com.metoo.nrsm.core.network.jopo.SnmpWalkResult;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestNetwork {

    // 通过SSH连接到192.168.6.101并读取192.168.6.1的主机名
    public String test(SnmpWalkResult snmpWalkResult) {
        String hostName = null;
        try {
            Session session = SnmpHelper.createSession();

            // 执行命令来获取目标主机的主机名
             String command = String.format("snmpwalk -%s -c %s %s 1.3.6.1.2.1.17.4.3.1.2",
                     snmpWalkResult.getVersion(), snmpWalkResult.getCommunity(), snmpWalkResult.getIp());

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
//                if (line.contains("STRING")) {
//                    hostName = line.split("STRING:")[1].trim();
//                }
                System.out.println(line);
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostName;
    }

    public static void main(String[] args) {
        TestNetwork client = new TestNetwork();

        SnmpWalkResult snmpWalkResult = new SnmpWalkResult()
                .setIp("192.168.6.1")
                .setVersion("v2c")
                .setCommunity("public@123");

        String result = client.test(snmpWalkResult);

    }

}
