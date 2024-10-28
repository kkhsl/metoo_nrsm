package com.metoo.nrsm.core.utils.ip.ipv4;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class IpSubnetMap {

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

    public static Long findSubnetForIp(Map<String, Long> subnetMap, String ipAddress) throws UnknownHostException {
        for (String subnet : subnetMap.keySet()) {
            if (isIpInSubnet(ipAddress, subnet)) {
                return subnetMap.get(subnet);  // 返回对应的值
            }
        }
        return null;  // 如果未找到匹配的网段
    }

    public static void main(String[] args) {
        try {
            Map<String, Long> subnetMap = new HashMap<>();
            subnetMap.put("192.168.6.0/24", 1l);
            subnetMap.put("192.168.7.0/24", 2l);
            subnetMap.put("10.0.0.0/8", 3l);

            String ipAddress = "192.168.6.50";
            Long result = findSubnetForIp(subnetMap, ipAddress);

            if (result != null) {
                System.out.println("IP " + ipAddress + " belongs to " + result);
            } else {
                System.out.println("IP " + ipAddress + " does not belong to any known subnet");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
