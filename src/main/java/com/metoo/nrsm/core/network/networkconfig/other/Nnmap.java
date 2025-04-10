package com.metoo.nrsm.core.network.networkconfig.other;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nnmap {
    private static final Pattern OS_DETAILS_PATTERN =
            Pattern.compile("OS details: (.+?)\n", Pattern.CASE_INSENSITIVE);
    private static final Pattern RUNNING_PATTERN =
            Pattern.compile("Running: (.+?)\n", Pattern.CASE_INSENSITIVE);
    private static final int TIMEOUT_SECONDS = 30;


    public static OsInfo detectWithNmap(String ip) {
        List<String> command = new ArrayList<>();
        command.add("nmap");
        command.add("-O");
        command.add("--osscan-guess");
        command.add(ip);

        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            Thread timeoutThread = startTimeoutTimer(process);

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            timeoutThread.interrupt();
            process.waitFor();

            return parseNmapOutput(output.toString());
        } catch (IOException | InterruptedException e) {
            return new OsInfo("Error", "N/A");
        }
    }

    private static OsInfo parseNmapOutput(String output) {
        String osDetails = "Unknown";
        String running = "Unknown";

        // 解析OS details
        Matcher detailMatcher = OS_DETAILS_PATTERN.matcher(output);
        if (detailMatcher.find()) {
            osDetails = detailMatcher.group(1).trim();
        }

        // 解析Running信息
        Matcher runningMatcher = RUNNING_PATTERN.matcher(output);
        if (runningMatcher.find()) {
            running = runningMatcher.group(1).trim();
        }

        // 提取内核版本
        String kernel = extractKernelVersion(osDetails);

        return new OsInfo(running, kernel);
    }

    private static String extractKernelVersion(String details) {
        if (details.matches(".*\\d+\\.\\d+.*")) {
            Matcher m = Pattern.compile("(\\d+\\.\\d+[\\d\\.]*)").matcher(details);
            return m.find() ? m.group(1) : "N/A";
        }
        return "N/A";
    }

    private static Thread startTimeoutTimer(Process process) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT_SECONDS * 1000L);
                process.destroy();
            } catch (InterruptedException ignored) {
            }
        });
        thread.start();
        return thread;
    }

    static class OsInfo {
        String osName;
        String kernelVersion;

        public OsInfo(String osName, String kernelVersion) {
            this.osName = osName;
            this.kernelVersion = kernelVersion;
        }
    }

    public static void main(String[] args) {
        // 使用NMAP检测
        /*OsInfo nmapInfo = detectWithNmap("192.168.6.101");
        System.out.println("OS Name: " + nmapInfo.osName);
        System.out.println("Kernel Version: " + nmapInfo.kernelVersion);*/
        Session session = null;
        ChannelExec channel = null;
        StringBuilder output = new StringBuilder();
        try {
            session = SnmpHelper.createSession();
            // 创建一个执行频道
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("nmap -O --osscan-limit 192.168.6.101");
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            // 获取命令输出
            InputStream in = channel.getInputStream();
            channel.connect();
            // 读取输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                OsInfo osInfo = parseNmapOutput(output.toString());
                System.out.println("OS Name: " + osInfo.osName);
                System.out.println("Kernel Version: " + osInfo.kernelVersion);
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
