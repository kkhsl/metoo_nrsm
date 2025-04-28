package com.metoo.nrsm.core.network.networkconfig.other.ipscanner;

import com.metoo.nrsm.core.network.concurrent.PingThreadPool;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners.ArpingScanner;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners.PingScanner;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

public class IpScannerManager {

    public static void scanner(String ip, int mask) {
        try {
            InetAddress baseIp = InetAddress.getByName(ip);
            long startIp = ipToLong(baseIp);
            long subnetSize = (long) Math.pow(2, 32 - mask);
            CountDownLatch latch = new CountDownLatch((int) (subnetSize - 2));

            for (long i = 1; i < subnetSize - 1; i++) {
                String targetIp = longToIp(startIp + i);
                try {
                    PingThreadPool.execute(new ArpingScanner(targetIp));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // 确保计数减少
                }
            }

            latch.await(); // 等待所有任务完成
        } catch (Exception e) {
            handleNetworkError(e);
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
