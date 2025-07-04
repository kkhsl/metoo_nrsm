package com.metoo.nrsm.core.network.networkconfig.other.ipscanner;

import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.entity.Subnet;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.concurrent.*;

/**
 * 1. 当前方案的问题
 * (1) 线程池与 Semaphore 的冗余控制
 * 你同时使用了：
 * <p>
 * Executors.newFixedThreadPool(10)：限制线程数=10。
 * <p>
 * Semaphore(50)：限制并发任务数=50。
 * <p>
 * 矛盾点：
 * <p>
 * 线程池只能同时运行 10 个任务，但 Semaphore 允许提交 50 个任务到队列，导致 队列堆积（最多积压 40 个任务）。
 * <p>
 * Semaphore 的 acquire() 会阻塞，但线程池队列满时也会阻塞（取决于 RejectedExecutionHandler）。
 * <p>
 * (2) 资源浪费
 * CountDownLatch 的计数器大小为 subnetSize - 2（如 /24 子网需等待 254 次），可能引发内存问题（大子网时）。
 * <p>
 * 线程池的 shutdown() 和 awaitTermination() 逻辑可能无法及时释放资源。
 * <p>
 * (3) 异常处理不足
 * PingTask 内部的异常未被捕获，可能导致 Semaphore 未释放或 CountDownLatch 未计数。
 */
@Slf4j
public class PingSubnet {
    private static final int TIMEOUT_MS = 1000;

//    public static void main(String[] args) {
//        String ip="192.168.4.0";
//        Integer mask=22;
//        try {
//            scanSubnet(ip, mask);
//        } catch (NumberFormatException e) {
//            System.err.println("Invalid mask format");
//        } catch (Exception e) {
//            System.err.println("Error: " + e.getMessage());
//        }
//    }


    public static void main(String[] args) {
        Subnet subnet = new Subnet();
        subnet.setIp("192.168.6.0");
        subnet.setMask(24);
        SNMPv2Request.pingSubnet(subnet.getIp(), Integer.parseInt(String.valueOf(subnet.getMask())));

    }

    private static final int THREAD_POOL_SIZE = 10;

    /**
     * 控制并发ip数量
     *
     * @param ip
     * @param mask
     */
    public static void scanSubnet(String ip, int mask) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        Semaphore limiter = new Semaphore(50);
        try {
            InetAddress baseIp = InetAddress.getByName(ip);
            long startIp = ipToLong(baseIp);
            long subnetSize = (long) Math.pow(2, 32 - mask);

            // 使用CountDownLatch等待所有任务完成
            CountDownLatch latch = new CountDownLatch((int) (subnetSize - 2));

            for (long i = 1; i < subnetSize - 1; i++) { // 排除网络地址和广播地址
                limiter.acquire(); // 控制并发

                String targetIp = longToIp(startIp + i);
                executor.execute(() -> {
                    try {
                        new PingTask(targetIp).run();
                    } catch (Exception e) {
                        log.error("Task failed for {}: {}", targetIp, e.getMessage());
                    } finally {
                        limiter.release();
                        latch.countDown();
                    }
                });
            }

            // 等待所有任务完成
            latch.await();
        } catch (Exception e) {
            handleNetworkError(e);
        } finally {
            executor.shutdown();  // 禁止新任务提交
            try {
                if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                    executor.shutdownNow();  // 强制终止剩余任务
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
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
                log.info("Pinging {} - {}", ip, isAlive ? "Success" : "Failed");
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