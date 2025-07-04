package com.metoo.nrsm.core.utils.ip.ipv4;

import com.metoo.nrsm.core.utils.ip.Ipv6.IPv6SubnetCheck;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Ipv6SubnetMap {


    public static Long findSubnetForIp(Map<String, Long> subnetMap, String ipAddress) throws UnknownHostException {
        for (String subnet : subnetMap.keySet()) {
            if (IPv6SubnetCheck.isInSubnet(ipAddress, subnet)) {
                return subnetMap.get(subnet);  // 返回对应的值
            }
        }
        return null;  // 如果未找到匹配的网段
    }

    public static void main(String[] args) {
        try {
            Map<String, Long> subnetMap = new HashMap<>();
            subnetMap.put("192.168.6.0/24", 1l);
            subnetMap.put("192.168.7.0/24", 2l);
            subnetMap.put("10.0.0.0/8", 3l);

            String ipAddress = "192.168.6.50";
            Long result = findSubnetForIp(subnetMap, ipAddress);

            if (result != null) {
                System.out.println("IP " + ipAddress + " belongs to " + result);
            } else {
                System.out.println("IP " + ipAddress + " does not belong to any known subnet");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
