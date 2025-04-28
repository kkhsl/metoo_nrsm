package com.metoo.nrsm.core.network.networkconfig.other.ipscanner;

import com.metoo.nrsm.core.network.concurrent.PingThreadPool;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners.ArpingScanner;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners.NmapScanner;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners.PingScanner;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class IpScannerManager {

    public static void main(String[] args) {
        scanTarget("192.168.0.8/24");
    }

    public static void scanner(String ip, int mask) {
        try {
            InetAddress baseIp = InetAddress.getByName(ip);
            long startIp = ipToLong(baseIp);
            long subnetSize = (long) Math.pow(2, 32 - mask);
            CountDownLatch latch = new CountDownLatch((int) (subnetSize - 2));

            for (long i = 1; i < subnetSize - 1; i++) {
                String targetIp = longToIp(startIp + i);
                try {
                    PingThreadPool.execute(new NmapScanner(targetIp, latch));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            latch.await(); // 等待所有任务完成
        } catch (Exception e) {
            handleNetworkError(e);
        }
    }

    @Test
    public void test(){
        String subnet = "192.168.8.0/24";

        scanTarget(subnet);

//        String ipRange = "192.168.0.8-192.168.0.10";
//
//        scanTarget(ipRange);
//
//        String ip = "192.168.0.8";
//
//        scanTarget(ip);
    }


    /**
     * 扫描给定目标（可以是单个 IP、IP 范围或 CIDR 网段）
     */
    public static void scanTarget(String target) {
        try {
            // 使用 CountDownLatch 来等待所有扫描任务完成
            CountDownLatch latch = null;

            // 判断目标类型，执行对应的扫描
            if (target.contains("/")) {
                // 如果目标是 CIDR 网段（例如：192.168.1.0/24），直接用 nmap 扫描整个网段
                log.info("Scanning CIDR range: {}", target);
                PingThreadPool.execute(new NmapScanner(target, latch));
            } else if (target.contains("-")) {
                // 如果目标是 IP 范围（例如：192.168.1.1-192.168.1.100），进行范围扫描
                String[] parts = target.split("-");
                String startIp = parts[0];
                String endIp = parts[1];

                InetAddress start = InetAddress.getByName(startIp);
                InetAddress end = InetAddress.getByName(endIp);
                long startIpLong = ipToLong(start);
                long endIpLong = ipToLong(end);

                latch = new CountDownLatch((int) (endIpLong - startIpLong + 1));  // 设置计数为范围内的 IP 地址数量

                log.info("Scanning IP range: {} - {}", startIp, endIp);

                // 遍历范围并进行扫描
                for (long i = startIpLong; i <= endIpLong; i++) {
                    String ip = longToIp(i);
                    PingThreadPool.execute(new NmapScanner(ip, latch));
                }
            } else {
                // 如果目标是单个 IP 地址（例如：192.168.1.1），直接扫描该 IP
                log.info("Scanning single IP: {}", target);
                PingThreadPool.execute(new NmapScanner(target, latch));
            }
            // 等待扫描任务完成
           if(latch != null){
               latch.await();
           }
        } catch (Exception e) {
            log.error("Error during scan: {}", e.getMessage());
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
