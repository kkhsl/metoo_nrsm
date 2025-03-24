package com.metoo.nrsm.core.network.networkconfig.test;

import java.io.IOException;

public class modifyDns {

    /**
     * modifydns.py
     * @param dns1
     * @param dns2
     */
    public static void changeDNS(String dns1, String dns2) {
        try {
            // 删除现有的 DNS 设置
            String command1 = "sed -i '/^ *DNS.*/d' /etc/systemd/resolved.conf";
            executeCommand(command1);

            // 添加新的 DNS 设置
            String command2 = "sed -i '/Resolve/a DNS=" + dns2 + "' /etc/systemd/resolved.conf";
            executeCommand(command2);
            String command3 = "sed -i '/Resolve/a DNS=" + dns1 + "' /etc/systemd/resolved.conf";
            executeCommand(command3);

            // 重启 DNS 服务
            String command4 = "systemctl restart systemd-resolved.service";
            executeCommand(command4);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        Process process = processBuilder.start();
        process.waitFor();
    }

    public static void main(String[] args) {
        // 示例：更改 DNS
        changeDNS("8.8.8.8", "8.8.4.4");
    }
} 