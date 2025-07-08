package com.metoo.nrsm.core.utils.ip.Ipv6;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-15 14:50
 */
public class TransIPv6 {

    public static String getFullIPv6(String ipv6) {
        //入参为::时，此时全为0
        if (ipv6.equals("::")) {
            return "0000:0000:0000:0000:0000:0000:0000:0000";
        }
        //入参已::结尾时，直接在后缀加0
        if (ipv6.endsWith("::")) {
            ipv6 += "0";
        }
        String[] arrs = ipv6.split(":");
        String symbol = "::";
        int arrleng = arrs.length;
        System.out.println(arrleng);
        while (arrleng < 8) {
            symbol += ":";
            arrleng++;
        }
        ipv6 = ipv6.replace("::", symbol);
        System.out.println("ipv6:" + ipv6);
        String fullip = "";
        for (String ip : ipv6.split(":")) {
            while (ip.length() < 4) {
                ip = "0" + ip;
            }
            fullip += ip + ':';
        }
        return fullip.substring(0, fullip.length() - 1);
    }

    public static String getShortIPv6(String ipv6) {
        String shortIP = "";
        ipv6 = getFullIPv6(ipv6);
        String[] arr = ipv6.split(":");
        //去掉每组数据前的0
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].replaceAll("^0{1,3}", "");
        }
        //最长的连续0
        String[] arr2 = arr.clone();
        for (int i = 0; i < arr2.length; i++) {
            if (!"0".equals(arr2[i])) {
                arr2[i] = "-";
            }
        }
        Pattern pattern = Pattern.compile("0{2,}");
        Matcher matcher = pattern.matcher(StringUtils.join(Arrays.asList(arr2), ""));
        String maxStr = "";
        int start = -1;
        int end = -1;
        while (matcher.find()) {
            if (maxStr.length() < matcher.group().length()) {
                maxStr = matcher.group();
                start = matcher.start();
                end = matcher.end();
            }
        }
        // 组合IPv6简写地址
        if (maxStr.length() > 0) {
            for (int i = start; i < end; i++) {
                arr[i] = ":";
            }
        }
        shortIP = StringUtils.join(Arrays.asList(arr), ":");
        shortIP = shortIP.replaceAll(":{2,}", "::");
        return shortIP;
    }


    public static void main(String[] args) {
        String ipv6 = "240e:380:11d:2:0:0:0:0";
        System.out.println(getFullIPv6(ipv6));
        System.out.println(getShortIPv6(ipv6));// 240e:380:11d:2::


    }
}
