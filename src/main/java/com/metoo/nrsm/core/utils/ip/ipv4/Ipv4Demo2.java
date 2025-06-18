package com.metoo.nrsm.core.utils.ip.ipv4;

import java.net.InetAddress;
import java.net.UnknownHostException;

// 判断ipv4是否属于某一网段
public class Ipv4Demo2 {

    public static boolean isIpInSubnet(String ipAddress, String subnet) throws UnknownHostException {
        String[] parts = subnet.split("/");
        String subnetAddress = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        InetAddress inetAddress = InetAddress.getByName(ipAddress);
        InetAddress subnetInetAddress = InetAddress.getByName(subnetAddress);

        byte[] addressBytes = inetAddress.getAddress();
        byte[] subnetBytes = subnetInetAddress.getAddress();

        // 转换子网掩码
        int mask = 0xFFFFFFFF << (32 - prefixLength);

        // 将子网和 IP 地址转换为整数
        int ipAddrInt = byteArrayToInt(addressBytes);
        int subnetInt = byteArrayToInt(subnetBytes);

        // 检查 IP 是否在子网中
        return (ipAddrInt & mask) == (subnetInt & mask);
    }

    // 将字节数组转换为整数
    private static int byteArrayToInt(byte[] bytes) {
        int result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            String ipAddress = "192.168.6.50";
            String subnet = "192.168.6.0/24";
            boolean result = isIpInSubnet(ipAddress, subnet);
            System.out.println("IP " + ipAddress + " belongs to subnet " + subnet + ": " + result);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
