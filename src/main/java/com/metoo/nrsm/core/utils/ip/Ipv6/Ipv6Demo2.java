package com.metoo.nrsm.core.utils.ip.Ipv6;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-25 15:12
 */
public class Ipv6Demo2 {

    public static String generateNetworkSegment(String ipv6Address, int prefixLength) {
        try {
            InetAddress address = InetAddress.getByName(ipv6Address);
            byte[] addressBytes = address.getAddress();

            // 使用prefixLength来确定网络前缀的位数，并将其置为0
            int bytePrefixLength = prefixLength / 8;
            int bitPrefixLength = prefixLength % 8;

            // 将字节前缀置为0
            for (int i = 0; i < bytePrefixLength; i++) {
                addressBytes[i] = 0;
            }

            // 将位前缀置为0
            if (bitPrefixLength > 0) {
                byte mask = (byte) (0xFF << (8 - bitPrefixLength));
                addressBytes[bytePrefixLength] = (byte) (addressBytes[bytePrefixLength] & mask);
            }

            // 将剩余的字节置为0
            for (int i = bytePrefixLength + 1; i < addressBytes.length; i++) {
                addressBytes[i] = 0;
            }

            // 将修改后的字节转换回IPv6地址的字符串形式
            StringBuilder sb = new StringBuilder();
            for (byte b : addressBytes) {
                sb.append(String.format("%02X", b));
            }

            String networkSegment = sb.toString();
            // 将32个字节转换为一个IPv6地址的文本形式
            return networkSegment.substring(0, 4) + ":" + networkSegment.substring(4, 8) + ":" +
                    networkSegment.substring(8, 12) + ":" + networkSegment.substring(12, 16) + ":" +
                    networkSegment.substring(16, 20) + ":" + networkSegment.substring(20, 24) + ":" +
                    networkSegment.substring(24, 28) + ":" + networkSegment.substring(28, 32);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String ipv6Address = "240e:0381:011d:4100:0000:0000:0000:000b";
        int prefixLength = 64; // 例如，生成前64位为网络前缀的网段
        String networkSegment = generateNetworkSegment(ipv6Address, prefixLength);
        System.out.println("Network Segment: " + networkSegment);
    }
}
