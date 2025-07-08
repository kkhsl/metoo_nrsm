package com.metoo.nrsm.core.network.snmp4j.network;

import com.github.pagehelper.util.StringUtil;

import java.io.IOException;
import java.net.*;

public class NetworkUtils {

    // 检查设备是否可达
    public static boolean isHostNameReachable(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            return address.isReachable(2000);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error checking reachability: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // 检查设备SNMP服务端口是否可用(TCP)
    public static boolean isSNMPPortOpen(String ipAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), 2000); // 超时设置为2秒
            return true;  // 如果连接成功
        } catch (IOException e) {
            System.err.println("Error connecting to " + ipAddress + ":" + port);
            return false;
        }
    }

    // 检查目标 IP 的 SNMP UDP 161 端口是否可用(UDP)
    public static boolean isPortAvailable(String ip, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(new InetSocketAddress(ip, port));
            return true; // 端口可用
        } catch (Exception e) {
            System.out.println("无法连接到端口: " + ip + ":" + port);
            return false; // 端口不可用
        }
    }

    public static void main(String[] args) {

//        // 192.168.6.1
//        System.out.println("192.168.6.1 主机是否可达：" + isHostNameReachable("192.168.6.1"));
//        // 192.168.6.1
//        System.out.println("192.168.6.1 端口是否可用：" + isSNMPPortOpen("192.168.6.1", 161));
//
//        // 192.168.6.101
//        System.out.println(isHostNameReachable("192.168.6.101 主机是否可达：" + "192.168.6.101"));
//
//        // 192.168.6.102
//        System.out.println(isHostNameReachable("192.168.6.102 主机是否可达：" + "192.168.6.102"));

        // 192.168.6.1
        System.out.println("192.168.6.1 端口是否可用：" + isSNMPPortOpen("192.168.6.1", 161));

        System.out.println("192.168.6.1 端口是否可用：" + isPortAvailable("192.168.6.1", 161));

    }
}
