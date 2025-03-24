package com.metoo.nrsm.core.network.networkconfig.other;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class processkill {
    private static final int TIMEOUT_SECONDS = 5;

    public static void main(String[] args) {
        String processPattern = "";   //dnsredis
        try {
            // 跨平台获取进程ID
            List<Integer> pids = getProcessIds(processPattern);

            if (pids.isEmpty()) {
                System.out.println("未找到匹配进程");
                System.exit(0);
            }

            // 终止所有匹配进程
            pids.forEach(pid -> {
                try {
                    terminateProcess(pid);
                    System.out.println("成功终止进程: " + pid);
                } catch (Exception e) {
                    System.err.println("终止进程失败: " + pid + " 原因: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("操作失败: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * 获取匹配进程ID列表（跨平台实现）
     */
    private static List<Integer> getProcessIds(String pattern) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return getWindowsProcessIds(pattern);
        } else {
            return getUnixProcessIds(pattern);
        }
    }

    /**
     * Windows 进程发现（使用WMIC命令）
     */
    private static List<Integer> getWindowsProcessIds(String pattern) throws Exception {
        List<Integer> pids = new ArrayList<>();

        // 构建WMIC命令
        String command = "wmic process where " +
                "\"commandline like '%" + pattern + "%'\" get processid";

        Process process = new ProcessBuilder("cmd.exe", "/c", command).start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            boolean skipHeader = true;
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
                        pids.add(Integer.parseInt(line));
                    } catch (NumberFormatException e) {
                        // 忽略无效行
                    }
                }
            }
        }

        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new Exception("WMIC命令执行超时");
        }

        return pids;
    }

    /**
     * Unix/Linux 进程发现（使用pgrep命令）
     */
    private static List<Integer> getUnixProcessIds(String pattern) throws Exception {
        List<Integer> pids = new ArrayList<>();

        Process process = new ProcessBuilder("pgrep", "-f", pattern).start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    pids.add(Integer.parseInt(line.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效行
                }
            }
        }

        if (process.waitFor() != 0) {
            return new ArrayList<>(); // 无匹配进程
        }

        return pids;
    }

    /**
     * 跨平台终止进程
     */
    private static void terminateProcess(int pid) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            Process killProcess = new ProcessBuilder("taskkill", "/F", "/PID",
                    String.valueOf(pid)).start();

            if (!killProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new Exception("终止进程超时");
            }

            if (killProcess.exitValue() != 0) {
                throw new Exception("终止失败，错误码: " + killProcess.exitValue());
            }

        } else {
            Process killProcess = new ProcessBuilder("kill", "-9",
                    String.valueOf(pid)).start();

            if (killProcess.waitFor() != 0) {
                throw new Exception("终止失败");
            }
        }
    }
}