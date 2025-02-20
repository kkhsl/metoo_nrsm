package com.metoo.nrsm.core.network.hostname;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.jopo.SnmpWalkResult;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SnmpWalkSshStrategy implements SnmpWalkStrategy {

    @Override
    public void execute(SnmpWalkResult snmpWalkResult) {
        // 远程执行通过 SSH 获取 SNMP 结果
        // 示例：使用 JSch 执行 SSH 命令
        String result = getHostNameViaSSH(snmpWalkResult);
        snmpWalkResult.setResult(result);
    }

    // 通过SSH连接到指定主机并读取目标主机的主机名
    private /*SnmpWalkResult*/String getHostNameViaSSH(SnmpWalkResult snmpWalkResult) {
        String hostName = null;
        String errorMessage = null;
        Session session = null;
        BufferedReader reader = null;

        try {
            session = SnmpHelper.createSession(); // 创建SSH连接
            reader = executeSnmpWalkCommand(session, snmpWalkResult); // 执行snmpwalk命令并获取结果流

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("STRING")) {
                    hostName = line.split("STRING:")[1].trim();
                }
            }

        } catch (Exception e) {
            errorMessage = "Error fetching host name: " + e.getMessage();
            e.printStackTrace();
        } finally {
            // 确保资源在使用完后被关闭
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        // 将获取的结果封装到 SnmpWalkResult 对象中
//        snmpWalkResult.setResult(hostName != null ? hostName : errorMessage);
//        return snmpWalkResult;
        return hostName;
    }

    // 执行 snmpwalk 命令并返回输出流
    private BufferedReader executeSnmpWalkCommand(Session session, SnmpWalkResult snmpWalkResult) throws JSchException, IOException {
        String command = String.format("snmpwalk -%s -c %s %s 1.3.6.1.2.1.1.5.0",
                snmpWalkResult.getVersion(), snmpWalkResult.getCommunity(), snmpWalkResult.getIp());

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);

        channel.connect();
        InputStream in = channel.getInputStream();
        return new BufferedReader(new InputStreamReader(in));
    }
}
