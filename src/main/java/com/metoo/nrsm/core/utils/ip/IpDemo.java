package com.metoo.nrsm.core.utils.ip;

import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-23 10:42
 */
public class IpDemo {


    public static void main(String[] args) {
        String mask = Ipv4Util.getMaskByMaskBit(23);
        System.out.println(mask);
        String v = Ipv4Util.getNetwork("192.168.5.0", mask);
        System.out.println(v);

//        String networkAddress = IpV4Util.getNetworkAddress("192.168.5.0",  23);
//        System.out.println(networkAddress);

        Map<String, String> a = Ipv4Util.getNetworkIp("192.168.5.0", mask);
        System.out.println(a.get("network"));

    }

}
