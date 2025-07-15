package com.metoo.nrsm.core.network.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.mapper.TrafficDataMapper;
import com.metoo.nrsm.entity.TrafficData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SshGetTrafficTest {

    private static final Logger logger = LoggerFactory.getLogger(SshGetTrafficTest.class);

    @Resource
    private static TrafficDataMapper trafficDataMapper;

    private static final int SSH_CONNECT_TIMEOUT = 30000; // 30 seconds
    private static final int COMMAND_TIMEOUT = 60000;     // 60 seconds

    private static void validateSessionState(Session session) throws JSchException {
        if (session == null) {
            throw new JSchException("Session is null");
        }

        if (session.isConnected()) {
            // 检查连接有效性
            try {
                session.sendIgnore();
            } catch (Exception e) {
                logger.warn("Existing connection is invalid, reconnecting...");
                session.disconnect();
                session.connect(SSH_CONNECT_TIMEOUT);
            }
        }
    }

    public static void getTraffic(int vlanId) {
        Session session = null;
        ChannelExec channel = null;
        BufferedReader reader = null;

        try {
            session = SnmpHelper.createSession();
            validateSessionState(session); // 新增状态验证

            String command = String.format("cat vlan%d.txt", vlanId);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            channel.connect(COMMAND_TIMEOUT);
            logger.debug("Channel connected for VLAN {}", vlanId);

            InputStream in = channel.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            parseTrafficData(result.toString(), vlanId);

        } catch (JSchException e) {
            handleJSchException(e, session);
        } catch (IOException e) {
            logger.error("IO error during command execution: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        } finally {
            closeResources(channel, session, reader);
        }
    }

    private static void handleJSchException(JSchException e, Session session) {
        if (e.getMessage().contains("session is already connected")) {
            logger.warn("Session reuse detected: {}", e.getMessage());
            // 执行清理操作
            if (session != null) {
                session.disconnect();
            }
        } else if (e.getMessage().contains("Auth fail")) {
            logger.error("SSH authentication failed: {}", e.getMessage());
        } else if (e.getMessage().contains("timeout")) {
            logger.error("SSH connection timeout: {}", e.getMessage());
        } else {
            logger.error("SSH connection error: {}", e.getMessage(), e);
        }
    }

    private static void closeResources(ChannelExec channel, Session session, BufferedReader reader) {
        try {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            logger.warn("Error closing resources: {}", e.getMessage());
        }
    }

    public static void parseTrafficData(String output, int vlanId) {
        try {
            String[] rates = new String[4]; // [ipv4In, ipv4Out, ipv6In, ipv6Out]
            Arrays.fill(rates, "0");

            String[] sections = output.split("(Ipv4:|Ipv6:)");
            for (int i = 1; i < sections.length; i++) {
                String section = sections[i].trim();
                boolean isIPv4 = i % 2 == 1; // sections[1]=IPv4, sections[2]=IPv6

                processSection(section, rates, isIPv4 ? 0 : 2);
            }

            saveToDatabase(vlanId, rates[0], rates[1], rates[2], rates[3]);

        } catch (NumberFormatException e) {
            logger.error("Invalid number format in controller data: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error parsing controller data: {}", e.getMessage(), e);
        }
    }

    private static void processSection(String section, String[] rates, int offset) {
        String[] lines = section.split("\n");
        for (String line : lines) {
            if (line.contains("Last 300 seconds input rate")) {
                rates[offset] = extractRate(line);
            } else if (line.contains("Last 300 seconds output rate")) {
                rates[offset + 1] = extractRate(line);
            }
        }
    }

    private static String extractRate(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length >= 4) {
            String rate = parts[parts.length - 4];
            return rate.matches("\\d+") ? rate : "0"; // 验证数字格式
        }
        return "0";
    }

    private static void saveToDatabase(int vlanId, String ipv4In, String ipv4Out, String ipv6In, String ipv6Out) {
        try {
            TrafficData data = new TrafficData();
            data.setAddTime(new Date());
            data.setVlanId(vlanId);
            data.setIpv4InputRate(ipv4In);
            data.setIpv4OutputRate(ipv4Out);
            data.setIpv6InputRate(ipv6In);
            data.setIpv6OutputRate(ipv6Out);

            trafficDataMapper.insertTrafficData(data);
        } catch (Exception e) {
            logger.error("Database operation failed: {}", e.getMessage(), e);

        }
    }

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // 定时任务配置
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        getTraffic(10);
                    } catch (Exception e) {
                        logger.error("Scheduled task execution failed: {}", e.getMessage());
                    }
                },
                0, 1, TimeUnit.MINUTES
        );

        // 优雅关闭处理
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down scheduler...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("Force shutdown scheduler");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Shutdown interrupted: {}", e.getMessage());
            }
        }));
    }
}