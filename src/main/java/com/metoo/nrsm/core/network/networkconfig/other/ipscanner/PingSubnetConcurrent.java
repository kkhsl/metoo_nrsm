package com.metoo.nrsm.core.network.networkconfig.other.ipscanner;

import com.metoo.nrsm.core.network.concurrent.PingThreadPool;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.entity.Subnet;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.concurrent.*;

@Slf4j
public class PingSubnetConcurrent {

    public static void main(String[] args) {
        Subnet subnet = new Subnet();
        subnet.setIp("192.168.6.0");
        subnet.setMask(24);
        SNMPv2Request.pingSubnetConcurrent(subnet.getIp(), Integer.parseInt(String.valueOf(subnet.getMask())));

    }

    private static final int TIMEOUT_MS = 1000;

    /**
     * 控制并发ip数量
     * 直接利用线程池的队列和拒绝策略控制并发，去掉冗余的 Semaphore 和 CountDownLatch：
     * @param ip
     * @param mask
     */
    public static void scanSubnet(String ip, int mask) {
        try {
            InetAddress baseIp = InetAddress.getByName(ip);
            long startIp = ipToLong(baseIp);
            long subnetSize = (long) Math.pow(2, 32 - mask);
            CountDownLatch latch = new CountDownLatch((int) (subnetSize - 2));

            for (long i = 1; i < subnetSize - 1; i++) {
                String targetIp = longToIp(startIp + i);
//                PingThreadPool.execute(() -> {
//                    try {
//                        new PingTask(targetIp).run();
//                    } catch (Exception e) {
//                        System.err.println("Ping failed for " + targetIp + ": " + e.getMessage());
//                    } finally {
//                        latch.countDown(); // 确保计数减少
//                    }
//                });
                PingThreadPool.execute(new PingTask(targetIp));
            }

            latch.await(); // 等待所有任务完成
        } catch (Exception e) {
            handleNetworkError(e);
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

            }  catch (IOException e) {
                log.error("Ping failed for {} (IO): {}", ip, e.getMessage());
            }/* catch (InterruptedException e) {
                log.error("Ping interrupted for {}: {}", ip, e.getMessage());
                Thread.currentThread().interrupt();  // 恢复中断状态
            } */catch (Exception e) {
                log.error("Unexpected ping error for {}: {}", ip, e.getMessage());
            }
        }

        private String[] buildPingCommand(String os, String ip) {
            if (os.contains("win")) {
                return new String[]{"ping", "-n", "1", "-w", String.valueOf(TIMEOUT_MS), ip};
            } else {
                return new String[]{"ping", "-c", "1", "-W", "1", ip};
            }
        }

        private boolean parsePingOutput(String os, Process process) {
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
            } catch (IOException e) {
                log.error("Ping failed for {} (IO): {}", ip, e.getMessage());
            } catch (InterruptedException e) {
                log.error("Ping interrupted for {}: {}", ip, e.getMessage());
                Thread.currentThread().interrupt();  // 恢复中断状态
            } catch (Exception e) {
                log.error("Unexpected ipscanner error for {}: {}", ip, e.getMessage());
            }
            return false;
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