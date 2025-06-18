package com.metoo.nrsm.core.system.conf.radvd.strategy;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.entity.Radvd;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
public class WindowsRadvdConfigUpdateStrategy implements RadvdConfigUpdateStrategy {

    private static final String REMOTE_HOST = "192.168.6.102"; // 远程Linux主机IP地址
    private static final String USERNAME = "root"; // 远程用户名
    private static final String PASSWORD = "Metoo89745000!"; // 远程用户密码

    private static final String UPDATE_CONFIG_COMMAND = "echo \"更新配置文件的命令或脚本\" > /etc/radvd.conf";

    @Override
    public void updateConfig(List<Radvd> radvdList) {
        try {
            String configContent = generateConfigContent(radvdList);
            executeRemoteCommand(configContent);
        } catch (Exception e) {
            System.err.println("远程更新配置文件失败: " + e.getMessage());
        }
    }

    // 生成配置文件内容
    private String generateConfigContent(List<Radvd> radvdList) {
        StringBuilder configContent = new StringBuilder();

        for (Radvd radvd : radvdList) {
            configContent.append("#").append(radvd.getName()).append("\n")
                    .append("interface ").append(radvd.getInterfaceName()).append(" {\n")
                    .append("    AdvSendAdvert on;\n")
                    .append("    MinRtrAdvInterval 30;\n")
                    .append("    MaxRtrAdvInterval 100;\n")
                    .append("    AdvManagedFlag on;\n")
                    .append("    AdvOtherConfigFlag on;\n")
                    .append("    AdvLinkMTU 1500;\n")
                    .append("    AdvDefaultLifetime 0;\n")
                    .append("    prefix ").append(radvd.getIpv6Prefix()).append(" {\n")
                    .append("        AdvOnLink on;\n")
                    .append("        AdvAutonomous on;\n")
                    .append("        AdvValidLifetime 86400;\n")
                    .append("        AdvPreferredLifetime 3600;\n")
                    .append("    };\n")
                    .append("};\n");
        }

        return configContent.toString();
    }

    // 执行远程命令
    private void executeRemoteCommand(String configContent) throws JSchException, IOException {
        Session session = null;
        ChannelExec channel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(USERNAME, REMOTE_HOST, 22);
            session.setPassword(PASSWORD);

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("echo \"" + configContent + "\" > /etc/radvd.conf");

            InputStream inputStream = channel.getInputStream();
            channel.connect();

            StringBuilder output = new StringBuilder();
            // 使用两个线程分别读取标准输出和错误输出
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    Thread.currentThread().interrupt();
                }
            });

            log.info("radvd 输出：{}", output);
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

    }
}