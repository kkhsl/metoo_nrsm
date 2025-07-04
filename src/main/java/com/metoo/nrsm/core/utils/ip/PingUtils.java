package com.metoo.nrsm.core.utils.ip;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-26 14:50
 * <p>
 * 1，创建ping工具
 * 2，实现ping方法
 * 3，调用ping方法
 * 4，输出ping结果
 */
public class PingUtils {

    public static void main(String[] args) {
        String ipAddress = "192.168.5.101"; // 需要Ping的IP地址
        ping(ipAddress);
    }


    /**
     * 测试地址是否可达
     *
     * @param ipAddress
     */
    public static void ping(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            // 设置超时时间为5000毫秒
            boolean isReachable = address.isReachable(5000);
            printResult(isReachable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void printResult(boolean isReachable) {
        if (isReachable) {
            System.out.println("主机可达");
        } else {
            System.out.println("主机不可达");
        }
    }
}
