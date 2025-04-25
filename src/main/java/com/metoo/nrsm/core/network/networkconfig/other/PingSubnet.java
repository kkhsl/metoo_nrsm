package com.metoo.nrsm.core.network.networkconfig.other;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.concurrent.*;

@Slf4j
public class PingSubnet {
    private static final int TIMEOUT_MS = 1000;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        String ip="192.168.4.0";
        Integer mask=22;
        try {
            scanSubnet(ip, mask);
        } catch (NumberFormatException e) {
            System.err.println("Invalid mask format");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * 控制并发ip数量
     * @param network
     * @param mask
     */
    public static void scanSubnet(String network, int mask) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        Semaphore limiter = new Semaphore(50);
        try {
            InetAddress baseIp = InetAddress.getByName(network);
            long startIp = ipToLong(baseIp);
            long subnetSize = (long) Math.pow(2, 32 - mask);

            // 使用CountDownLatch等待所有任务完成
            CountDownLatch latch = new CountDownLatch((int)(subnetSize - 2));

            for (long i = 1; i < subnetSize - 1; i++) { // 排除网络地址和广播地址
                limiter.acquire(); // 控制并发

                String targetIp = longToIp(startIp + i);
                executor.execute(() -> {
                    try {
                        new PingTask(targetIp).run();
                    } finally {
                        limiter.release();
                        latch.countDown();
                    }
                });
            }

            // 等待所有任务完成
            latch.await();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (Exception e) {
            handleNetworkError(e);
        } finally {
            executor.shutdownNow(); // 确保线程池被关闭
        }
    }
    static class PingTask implements Runnable {
        private final String ip;

        PingTask(String ip) {
            this.ip = ip;
        }

        @Override
        public void run() {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                String[] cmd = buildPingCommand(os, ip);

                Process process = new ProcessBuilder(cmd).start();
                boolean isAlive = parsePingOutput(os, process);

//                log.info("Pinging {} - {}", ip, isAlive ? "Success" : "Failed");

            } catch (Exception e) {
                 log.info("Ping error: " + e.getMessage());
            }
        }

        private String[] buildPingCommand(String os, String ip) {
            if (os.contains("win")) {
                return new String[]{"ping", "-n", "1", "-w", String.valueOf(TIMEOUT_MS), ip};
            } else {
                return new String[]{"ping", "-c", "1", "-W", "1", ip};
            }
        }

        private boolean parsePingOutput(String os, Process process) throws IOException {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (os.contains("win") && line.contains("bytes=32")) {
                        return true;
                    } else if (line.contains("1 packets transmitted") && line.contains("0% packet loss")) {
                        return true;
                    }
                }
                return process.waitFor() == 0;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    // IP地址转换工具方法
    private static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    private static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }

    private static void handleNetworkError(Exception e) {
        if (e instanceof ArrayIndexOutOfBoundsException) {
            System.err.println("Invalid IP address format");
        } else {
            System.err.println("Network error: " + e.getClass().getSimpleName());
        }
    }
}