package com.metoo.nrsm.core.utils.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class Ipv6Utils {

//    public static void main(String[] args) {
//
//        String ip = "192.168.1.1";
//        System.out.println(ip = "[" + ip.concat("]"));
//        try {
//            String[] addresses = {
//                    "240e:670:3e02:6"
//            };
//
//            for (String address : addresses) {
//                if (isValidIPv6(address)) {
//                    InetAddress inetAddress = InetAddress.getByName(address);
//                    System.out.println("Standardized address: " + inetAddress.getHostAddress());
//                } else {
//                    System.out.println("Invalid IPv6 address: " + address);
//                }
//            }
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//    }



    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:)|" +
                    "([0-9a-fA-F]{1,4}:){1,7}:|" +
                    "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|:" +
                    "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|:" +
                    "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|:" +
                    "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|:" +
                    "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|:" +
                    ":((:[0-9a-fA-F]{1,4}){1,6}|:)|" +
                    "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}" +
                    "((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){1,3}" +
                    "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)|" +
                    "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,4}"
    );

    /**
     * 验证 IPv6 地址格式
     */
    public static boolean isValidIPv6(String address) {
        return IPV6_PATTERN.matcher(address).matches();
    }

    private static final String IPV6_CIDR_REGEX =
            "^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::1|([0-9a-fA-F]{1,4}:){1,4}:([0-9]{1,3}\\.){3}[0-9]{1,3})/([0-9]{1,3})$";

    private static final Pattern pattern = Pattern.compile(IPV6_CIDR_REGEX);

    public static boolean isValidIPv6CIDR(String cidr) {
        return pattern.matcher(cidr).matches();
    }


//    public static void main(String[] args) {
//        String testCidr = "2001:db8::/32";
//        System.out.println("Is valid IPv6 CIDR: " + isValidIPv6CIDR(testCidr));
//    }

    public static boolean isIPv6InCIDR(String ipAddress, String cidr) {
        try {
            // 分离 IP 地址和前缀长度
            String[] parts = cidr.split("/");
            String network = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            // 将 IP 地址和网络地址转换为字节数组
            byte[] ipBytes = InetAddress.getByName(ipAddress).getAddress();
            byte[] networkBytes = InetAddress.getByName(network).getAddress();

            // 检查前缀长度
            if (ipBytes.length != networkBytes.length) {
                return false;
            }

            // 比较前缀部分
            int byteCount = prefixLength / 8;
            int bitCount = prefixLength % 8;

            // 比较完整字节
            for (int i = 0; i < byteCount; i++) {
                if (ipBytes[i] != networkBytes[i]) {
                    return false;
                }
            }

            // 如果还有剩余的位，检查它们
            if (bitCount > 0) {
                int mask = (1 << (8 - bitCount)) - 1;
                if ((ipBytes[byteCount] & ~mask) != (networkBytes[byteCount] & ~mask)) {
                    return false;
                }
            }

            return true;

        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        String ipAddress = "240E:381:119:C400:C9C:FDFF:FEC3:769F";
        String cidr = "240E:381:119:C400::/64";

        boolean isInCidr = isIPv6InCIDR(ipAddress, cidr);
        System.out.println(ipAddress + " 在 " + cidr + " 范围内: " + isInCidr);
    }
}
