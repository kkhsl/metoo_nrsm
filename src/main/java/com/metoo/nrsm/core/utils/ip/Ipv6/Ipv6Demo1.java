package com.metoo.nrsm.core.utils.ip.Ipv6;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-25 15:06
 */
public class Ipv6Demo1 {


    // 将IPv6地址转换为bits
    private static String toBinary(String ipv6Address) {
        String[] parts = ipv6Address.split(":");
        StringBuilder binaryBuilder = new StringBuilder();
        for (String part : parts) {
            String binary = Integer.toBinaryString(Integer.parseInt(part, 16));
            binaryBuilder.append(String.format("%1$16s", binary).replace(' ', '0'));
        }
        return binaryBuilder.toString();
    }

    // 获取网段
    private static String getNetworkSegment(String ipv6Address, int prefixLength) {
        String binaryAddress = toBinary(ipv6Address);
        return binaryAddress.substring(0, prefixLength);
    }

    public static void main(String[] args) {
        String ipv6Address = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        int prefixLength = 32; // 举例使用32位前缀长度

        ipv6Address = "240e:0381:011b:0804:0000:0000:0000:0001";
        prefixLength = 32; // 举例使用32位前缀长度


        String networkSegment = getNetworkSegment(ipv6Address, prefixLength);

        System.out.println("网段: " + networkSegment);

        String splitNumber = splitEveryFourDigits(networkSegment);

        System.out.println(splitNumber);
    }


    public static String splitEveryFourDigits(String number) {
        return String.join(":", number.replaceAll("(.{4})", "$1:").trim().split(":"));
    }


}
