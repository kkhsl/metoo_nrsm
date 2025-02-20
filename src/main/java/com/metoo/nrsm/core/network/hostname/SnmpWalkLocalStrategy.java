package com.metoo.nrsm.core.network.hostname;

import com.metoo.nrsm.core.network.jopo.SnmpWalkResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SnmpWalkLocalStrategy implements SnmpWalkStrategy {

    @Override
    public void execute(SnmpWalkResult result) {
        // 本地执行 SNMP 命令获取结果
        String hostName = getHostNameViaLocalSnmpWalk(result);
        result.setResult(hostName);
    }

    private String getHostNameViaLocalSnmpWalk(SnmpWalkResult snmpWalkResult) {
        // 本地执行 SNMP 命令获取主机名的逻辑
        // 通过本地执行 snmpwalk 命令
        // 执行本地 snmpwalk 命令
        String result = "";
        try {
            // 构建 snmpwalk 命令
            String command = String.format("snmpwalk -%s -c%s %s 1.3.6.1.2.1.1.5.0",
                    snmpWalkResult.getVersion(), snmpWalkResult.getCommunity(), snmpWalkResult.getIp());

            // 使用 ProcessBuilder 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);  // 将错误流与输出流合并
            Process process = processBuilder.start();

            // 获取命令的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 获取命令执行的结果
            result = output.toString();
            process.waitFor(); // 等待命令执行完成
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
