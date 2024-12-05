package com.metoo.nrsm.core.utils.net;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 19:08
 */
public class IPSortExample {


    // 将 IP 地址转换为整数
    public static long ipToLong(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        return (Long.parseLong(parts[0]) << 24)
                + (Long.parseLong(parts[1]) << 16)
                + (Long.parseLong(parts[2]) << 8)
                + Long.parseLong(parts[3]);
    }
}
