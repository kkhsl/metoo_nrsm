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
        if(ipv6Cidr.contains("/")){
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

    public static boolean verifyIpv62(String ipv6Address) throws RuntimeException {

        Pattern ipv6Pattern = Pattern.compile(
                "([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|" +
                        "([0-9a-fA-F]{1,4}:){1,7}:|" +
                        "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
                        "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
                        "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
                        "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
                        "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
                        "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
                        ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +
                        "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|" +
                        "::(ffff(:0{1,4}){0,1}:){0,1}" +
                        "(([0-9]{1,3}\\.){3,3}[0-9]{1,3})|" +
                        "([0-9a-fA-F]{1,4}:){1,4}:" +
                        "([0-9]{1,3}\\.){3,3}[0-9]{1,3})");
        Matcher matcher = ipv6Pattern.matcher(ipv6Address);
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
    public void convertMaskBitsToMaskTest(){
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



}
