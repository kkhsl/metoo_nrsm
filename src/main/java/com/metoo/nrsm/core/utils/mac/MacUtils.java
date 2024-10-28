package com.metoo.nrsm.core.utils.mac;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-24 11:13
 */
public class MacUtils {


    public static void main(String[] args) {
        String macWithoutColons = "24cf24805b30";
        String macWithColons = formatMacAddress(macWithoutColons);
        System.out.println("Formatted MAC Address: " + macWithColons);
    }

    public static String formatMacAddress(String mac) {
        // 验证输入是否合法
        if (mac == null || mac.length() != 12) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }

        // 将 MAC 地址按每两个字符插入一个冒号
        StringBuilder formattedMac = new StringBuilder();
        for (int i = 0; i < mac.length(); i += 2) {
            if (i > 0) {
                formattedMac.append(":");
            }
            formattedMac.append(mac.substring(i, i + 2));
        }

        return formattedMac.toString();
    }
}
