package com.metoo.nrsm.core.utils.ip.Ipv6;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-25 15:32
 */
public class Ipv6Demo3 {

    public static String generateNetwork(String ipv6Address, int prefixLength) {
        try {
            InetAddress address = InetAddress.getByName(ipv6Address);
            if (address instanceof Inet6Address) {
                byte[] addressBytes = address.getAddress();
                int netmaskLength = 8 * addressBytes.length;
                int hostIdLength = netmaskLength - prefixLength;

                // 掩码
                String networkMask = "";
                for (int i = 0; i < netmaskLength; i++) {
                    networkMask += (i < prefixLength) ? '1' : '0';
                }

                // 网段
                String networkAddress = "";
                for (int i = 0; i < addressBytes.length; i++) {
                    String byteStr = Integer.toBinaryString(addressBytes[i] & 0xFF);
                    networkAddress += String.join("", Collections.nCopies(8 - byteStr.length(), "0")); // 补零
                    networkAddress += byteStr;
                }

                // 去掉主机部分
                networkAddress = networkAddress.substring(0, networkAddress.length() - hostIdLength);

                // 添加掩码
                networkAddress += networkMask;

                // 转换回IPv6格式
                networkAddress = convertBitsToHex(networkAddress);

                return networkAddress;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String convertBitsToHex(String bits) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < bits.length(); i += 4) {
            String sub = bits.substring(i, i + 4);
            hex.append(Integer.parseInt(sub, 2));
            if ((i / 4) % 2 == 1 && i < bits.length() - 4) {
                hex.append(":");
            }
        }
        return hex.toString();
    }

    public static void main(String[] args) {
        String ipv6Address = "3ffe:3201:1401:1280:c8ff:fe4d:db39:1984";
        int prefixLength = 32; // 例如 /48
        String network = generateNetwork(ipv6Address, prefixLength);
        System.out.println("Network: " + network);
    }
}
