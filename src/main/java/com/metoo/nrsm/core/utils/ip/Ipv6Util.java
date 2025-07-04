package com.metoo.nrsm.core.utils.ip;

import com.metoo.nrsm.core.utils.ip.Ipv6.TransIPv6;
import org.junit.Test;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-22 16:31
 */
public class Ipv6Util {

    private static final String IPV6_ADDRESS = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";
    private static final String IPv6_SLASH_FORMAT = IPV6_ADDRESS + "/(\\d{1,3})"; // 0 -> 32
    private static final Pattern Ipv6AddressPattern = Pattern.compile(IPV6_ADDRESS);
    private static final Pattern Ipv6CidrPattern = Pattern.compile(IPv6_SLASH_FORMAT);

    public static void main(String[] args) {
        String ipv6 = "240e:380:11d:2::";
        System.out.println(verifyIpv6(ipv6));

//        240e:380:11d:2::/64
        String ipv6Cidr = "240e:380:11d:2:adx:/64";
        if (ipv6Cidr.contains("/")) {
            String subnet = ipv6Cidr.split("/")[0];

            System.out.println("subnet 格式 ：" + Ipv6Util.verifyIpv6(subnet));

            String mask = ipv6Cidr.split("/")[1];
            String fillIpv6 = TransIPv6.getFullIPv6(subnet);

            boolean isValid = verifyCidr(fillIpv6 + "/" + mask);
            System.out.println("Is valid IPv6 CIDR: " + isValid);


            boolean flag = verifyCidr("240e:380:11d:2::/64");
            System.out.println("Is valid IPv6 CIDR: " + flag);
        }

        System.out.println(verifyIpv6("2400:3200::1"));

    }

    /**
     * 校验Ipv6地址
     *
     * @param ipv6Address
     * @return
     * @throws RuntimeException
     */
    public static boolean verifyIpv6(String ipv6Address) throws RuntimeException {
        Matcher matcher = Ipv6AddressPattern.matcher(ipv6Address);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    private static final String IPV6_CIDR_PATTERN =
            "(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +  // Full IPv6 address
                    "([0-9a-fA-F]{1,4}:){1,7}:|" +               // Abbreviated form with ::
                    "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
                    "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
                    "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
                    "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
                    "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
                    "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
                    ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +
                    "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|" +
                    "::(ffff(:0{1,4}){0,1}:){0,1}" +
                    "((25[0-5]|2[0-4][0-9]|[0-1]{0,1}[0-9]{0,1}[0-9])\\.){3,3}" +
                    "(25[0-5]|2[0-4][0-9]|[0-1]{0,1}[0-9]{0,1}[0-9])|" +
                    "([0-9a-fA-F]{1,4}:){1,4}:" +
                    "((25[0-5]|2[0-4][0-9]|[0-1]{0,1}[0-9]{0,1}[0-9])\\.){3,3}" +
                    "(25[0-5]|2[0-4][0-9]|[0-1]{0,1}[0-9]{0,1}[0-9]))" +
                    "/([0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8])";

    private static final Pattern pattern = Pattern.compile(IPV6_CIDR_PATTERN);


    public static boolean verifyCidr(String ipv6Cidr) {
//        String regex = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}/\\d{1,3}$";
//        return ipv6Cidr.matches(regex);

        return pattern.matcher(ipv6Cidr).matches();
    }


//
//    public static String ipv6AddressFormat(String ipv6Address) throws RuntimeException {
//        Matcher matcher = Ipv6AddressPattern.matcher(ipv6Address);
//        if (matcher.matches()) {
//            try {
//                String ipv6Str = Inet6Address.getByName(ipv6Address).getHostAddress();
//                System.out.println(ipv6Str);
//                return ipv6Str.replaceAll("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)", "::$2");
//            } catch (UnknownHostException e) {
//                throw new ValidationException("this ip is not valid v6 address");
//            }
//        } else {
//            throw new ValidationException("this ip is not valid v6 address");
//        }
//    }
//
//    public static String ipv6PrefixFormat(String ipv6Prefix) throws RuntimeException {
//        Matcher matcher = Ipv6CidrPattern.matcher(ipv6Prefix);
//        if (matcher.matches()) {
//            String ip = ipv6Prefix.split("/")[0];
//            String len = ipv6Prefix.split("/")[1];
//            try {
//                String ipv6Str = Inet6Address.getByName(ip).getHostAddress();
//                return ipv6Str.replaceAll("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)", "::$2") + "/" + len;
//            } catch (UnknownHostException e) {
//                throw new ValidationException("this prefix is not valid v6 prefix");
//            }
//        } else {
//            throw new ValidationException("this prefix is not valid v6 prefix");
//        }
//    }

    @Test
    public void convertMaskBitsToMaskTest() {
        int maskBits = 64; // 例如，64位掩码
        String mask = convertMaskBitsToMask(maskBits);
        System.out.println("Mask (bits): " + maskBits + " - Mask (hex): " + mask);
    }

    public static String convertMaskBitsToMask(int maskBits) {
        if (maskBits < 0 || maskBits > 128) {
            throw new IllegalArgumentException("Mask bits must be between 0 and 128");
        }

        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int bitsLeft = 16 - i * 16;
            int bitsToSet = (bitsLeft > maskBits) ? maskBits : bitsLeft;
            int value = (bitsToSet == 16) ? 0 : (1 << bitsToSet) - 1;
            mask.append(String.format("%04x", value));
            maskBits -= bitsToSet;
            if (maskBits <= 0) {
                break;
            }
        }

        return mask.toString().replaceAll("(^|:)(0+)", "$1").replaceAll("(:0{1,4}){2,}", "::");
    }


    private static byte[] applyMask(byte[] ipBytes, int mask) {
        byte[] masked = new byte[16];
        System.arraycopy(ipBytes, 0, masked, 0, 16);

        int bitsToClear = 128 - mask;
        for (int i = 15; i >= 0 && bitsToClear > 0; i--) {
            int clear = Math.min(bitsToClear, 8);
            masked[i] &= (0xFF << (8 - clear));
            bitsToClear -= clear;
        }
        return masked;
    }

    private static byte[] ipv6ToBytes(String ipv6) {
        try {
            return Inet6Address.getByName(ipv6).getAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * 校验是否为合法IPv6网络地址（主机位全0）
     *
     * @param fullIPv6 完整格式IPv6地址（如 20010db8000000000000000000000000）
     * @param mask     掩码位数
     * @return 是否满足网络地址规则
     */
    public static boolean isNetworkAddress(String fullIPv6, int mask) {
        // Step1: 转换为128位二进制字符串
        String binaryStr = ipv6ToBinary(fullIPv6);
        if (binaryStr == null || binaryStr.length() != 128) return false;

        // Step2: 应用掩码（主机位置0）
        String networkPart = binaryStr.substring(0, mask);
        String hostPart = binaryStr.substring(mask).replaceAll("[01]", "0");
        String expectedBinary = networkPart + hostPart;

        // Step3: 对比处理后的二进制是否与原地址一致
        return binaryStr.equals(expectedBinary);
    }

    /**
     * 将完整IPv6地址转为128位二进制字符串
     * （示例：20010db8000000000000000000000000 → 0010000000000001...）
     */
    private static String ipv6ToBinary(String fullIPv6) {
        try {
            byte[] bytes = Inet6Address.getByName(fullIPv6).getAddress();
            StringBuilder binaryStr = new StringBuilder();
            for (byte b : bytes) {
                binaryStr.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            }
            return binaryStr.toString();
        } catch (UnknownHostException e) {
            return null;
        }
    }


}
