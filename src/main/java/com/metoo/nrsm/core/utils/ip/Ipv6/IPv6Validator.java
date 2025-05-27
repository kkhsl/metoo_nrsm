package com.metoo.nrsm.core.utils.ip.Ipv6;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv6Validator {

    /**
     * 验证 IPv6 地址格式（带前缀长度）
     * @param ipv6Address 要验证的地址（如 "fc00:1000:0:4::1/64"）
     * @return 验证结果（错误信息为null表示格式正确）
     */
    public static String validateIPv6Format(String ipv6Address) {
        // 1. 检查是否包含前缀长度
        if (!ipv6Address.contains("/")) {
            return "错误：IPv6地址必须包含前缀长度（如/64）";
        }

        String[] parts = ipv6Address.split("/");
        if (parts.length != 2) {
            return "错误：格式应为'IPv6地址/前缀长度'";
        }

        String ipPart = parts[0];
        String prefixPart = parts[1];

        // 2. 验证前缀长度
        try {
            int prefixLen = Integer.parseInt(prefixPart);
            if (prefixLen < 0 || prefixLen > 128) {
                return "错误：前缀长度必须在0-128之间";
            }
        } catch (NumberFormatException e) {
            return "错误：前缀长度必须是整数";
        }

        // 3. 验证IPv6地址部分
        try {
            // 使用Java标准库验证
            InetAddress inetAddress = InetAddress.getByName(ipPart);
            if (!(inetAddress instanceof Inet6Address)) {
                return "错误：这不是有效的IPv6地址";
            }

            // 额外检查压缩格式（::）
            if (ipPart.contains(":::")) {
                return "错误：IPv6地址中只能使用一个'::'";
            }

            // 检查每段长度
            String normalized = inetAddress.getHostAddress(); // 展开格式
            String[] hextets = normalized.split(":");
            for (String hextet : hextets) {
                if (hextet.length() > 4) {
                    return "错误：IPv6每段不得超过4个字符";
                }
            }

        } catch (UnknownHostException e) {
            return "错误：无效的IPv6地址格式";
        }

        return null; // 表示验证通过
    }

    // 测试用例
    public static void main(String[] args) {
        String[] testCases = {
                "fc00:1000:0:4::1/64",    // 正确
                "fc00:1000:0:4::1",       // 缺少前缀
                "fc00:1000::4::1/64",     // 多个::
                "fc00:10000:0:4::1/64",   // 段长度超限
                "fc00:1000:0:4::1/200",   // 前缀超限
                "192.168.1.1/24"          // IPv4地址
        };

        for (String testCase : testCases) {
            String result = validateIPv6Format(testCase);
            System.out.printf("%-25s => %s\n",
                    testCase,
                    result == null ? "✅ 格式正确" : "❌ " + result
            );
        }
    }
}
