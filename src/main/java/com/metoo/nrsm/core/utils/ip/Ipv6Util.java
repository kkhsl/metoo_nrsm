package com.metoo.nrsm.core.utils.ip;

import com.sun.tools.internal.ws.wsdl.framework.ValidationException;

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
        String ipv6 = "240e:3880::9";
        boolean a = verifyIpv6(ipv6);
        System.out.println(a);

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
}
