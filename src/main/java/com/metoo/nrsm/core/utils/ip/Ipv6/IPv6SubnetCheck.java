package com.metoo.nrsm.core.utils.ip.Ipv6;

import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.entity.User;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-15 14:15
 */
public class IPv6SubnetCheck {

    public static void main(String[] args) {
        String ipAddress = "240b:8150:3008:a00:7fac:2d8d:1fe1:3e3d";
        String network = "240B:8150:3008:0A00::0/64";

        boolean isInNetwork = isInSubnet(ipAddress, network);
        System.out.println("Is the IP address in the network? " + isInNetwork);
    }

    public static boolean isInSubnet(String ipAddress, String network) {
        try {
            String[] parts = network.split("/");
            InetAddress ip = InetAddress.getByName(ipAddress);
            InetAddress net = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] ipBytes = ip.getAddress();
            byte[] netBytes = net.getAddress();

            // Check if the prefix length is valid
            if (prefixLength < 0 || prefixLength > 128) {
                throw new IllegalArgumentException("Invalid prefix length");
            }

            // Compare the relevant bits
            int byteCount = prefixLength / 8;
            int bitCount = prefixLength % 8;

            // Compare full bytes
            for (int i = 0; i < byteCount; i++) {
                if (ipBytes[i] != netBytes[i]) {
                    return false;
                }
            }

            // Compare the remaining bits
            if (bitCount > 0) {
                int mask = 0xFF00 >> bitCount;
                if ((ipBytes[byteCount] & mask) != (netBytes[byteCount] & mask)) {
                    return false;
                }
            }

            return true;

        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }




    /**
     * 静态内部类：封装 IPv6 地址和前缀长度的解析结果
     */
    private static class IPv6Network {
        final Inet6Address address;
        final int prefixLen;

        IPv6Network(String ipWithPrefix) throws UnknownHostException, NumberFormatException {
            String[] parts = ipWithPrefix.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format. Expected 'IPv6/PrefixLen'");
            }
            this.address = (Inet6Address) InetAddress.getByName(parts[0]);
            this.prefixLen = Integer.parseInt(parts[1]);
            if (prefixLen < 0 || prefixLen > 128) {
                throw new IllegalArgumentException("Prefix length must be 0-128");
            }
        }
    }

    // 缓存解析结果（可选优化）
    private static final Map<String, IPv6Network> cache = new HashMap<>();

    /**
     * 检查两个 IPv6 地址是否属于同一子网
     */
    public static boolean isInSameSubnet(String radvdPrefix, String interfaceAddress) {
        try {
            IPv6Network radvd = parseIPv6Network(radvdPrefix);
            IPv6Network iface = parseIPv6Network(interfaceAddress);

            // 1. 前缀长度必须一致
            if (radvd.prefixLen != iface.prefixLen) {
                return false;
            }

            // 2. 比较网络部分
            return compareNetworkBytes(radvd.address, iface.address, radvd.prefixLen);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 解析 IPv6 地址（带缓存优化）
     */
    private static IPv6Network parseIPv6Network(String ipWithPrefix) throws Exception {
        if (!cache.containsKey(ipWithPrefix)) {
            cache.put(ipWithPrefix, new IPv6Network(ipWithPrefix));
        }
        return cache.get(ipWithPrefix);
    }

    /**
     * 比较两个 IPv6 地址的网络部分
     */
    private static boolean compareNetworkBytes(Inet6Address ip1, Inet6Address ip2, int prefixLen) {
        byte[] bytes1 = ip1.getAddress();
        byte[] bytes2 = ip2.getAddress();
        int fullBytes = prefixLen / 8;

        // 1. 比较完整字节
        for (int i = 0; i < fullBytes; i++) {
            if (bytes1[i] != bytes2[i]) {
                return false;
            }
        }

        // 2. 处理剩余位数（非整字节）
        int remainingBits = prefixLen % 8;
        if (remainingBits > 0) {
            int mask = 0xFF << (8 - remainingBits);
            return (bytes1[fullBytes] & mask) == (bytes2[fullBytes] & mask);
        }

        return true;
    }

    // 测试用例
    @Test
    public void main() {
        // 正常情况
        String radvdPrefix = "fc00:1000:0:100::/64";
        String interfaceAddress1 = "fc00:1000:0:100::3/64"; // 同一子网
        String interfaceAddress2 = "fc00:1000:0:200::3/64"; // 不同子网

        System.out.println(isInSameSubnet(radvdPrefix, interfaceAddress1)); // true
        System.out.println(isInSameSubnet(radvdPrefix, interfaceAddress2)); // false

        // 边界测试
        System.out.println(isInSameSubnet("::/0", "::1/0"));   // true (全匹配)
        System.out.println(isInSameSubnet("fd00::/8", "fd01::/8")); // true (前8位匹配)
        System.out.println(isInSameSubnet("fc00::/64", "fc00::1/128")); // false (前缀长度不等)
    }

}
