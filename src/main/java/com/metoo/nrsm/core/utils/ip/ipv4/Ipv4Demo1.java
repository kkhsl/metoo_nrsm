package com.metoo.nrsm.core.utils.ip.ipv4;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-25 15:24
 */
public class Ipv4Demo1 {

    //掩码是否是连续1判断
    private boolean IsSubnetMask(String netmask) {
        String[] masks = netmask.split("\\.");
        int mask = (Integer.parseInt(masks[0]) << 24) | (Integer.parseInt(masks[1]) << 16) | (Integer.parseInt(masks[2]) << 8) | Integer.parseInt(masks[3]);
        mask = ~mask + 1;
        if ((mask & (mask - 1)) == 0) {
            return true;
        } else {
            return false;
        }
    }

    //是否是有效的IP地址
    public boolean IsIpv4(String ipv4) {
        if (ipv4 == null || ipv4.length() == 0) {
            return false;
        }
        String[] parts = ipv4.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (int i = 0; i < parts.length; ++i) {
            try {
                int n = Integer.parseInt(parts[i]);
                if (n >= 0 && n <= 255) continue;
                return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    //根据IP与掩码获取网络号（环回地址）和广播地址
    private Map<String, String> getNetwork(String address, String netmask) {
        Map<String, String> map = new HashMap<String, String>();
        String network = new String();
        String broadcast = new String();
        String[] addresses = address.split("\\.");
        String[] masks = netmask.split("\\.");
        for (int i = 0; i < 4; i++) {
            int opmasksegement = ~Integer.parseInt(masks[i]) & 0xFF;
            //此处有坑，正常的int有32位，如果此数没有32位的话，就会用0填充前面的数，从而导致取反0的部分会用1来填充，用上述方法可以获取想要的部分
            int netsegment = Integer.parseInt(addresses[i]) & Integer.parseInt(masks[i]);
            network = network + String.valueOf(netsegment) + ".";
            broadcast = broadcast + String.valueOf(opmasksegement | netsegment) + ".";
        }
        map.put("network", network.substring(0, network.length() - 1));
        map.put("broadcast", broadcast.substring(0, broadcast.length() - 1));
        return map;
    }


    //判断IP是否在某个网段内
    private boolean isInRange(String ip, String netmask, String network) {
        String[] ips = ip.split("\\.");
        int ipAddr = (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16) | (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
        String[] masks = netmask.split("\\.");
        int mask = (Integer.parseInt(masks[0]) << 24) | (Integer.parseInt(masks[1]) << 16) | (Integer.parseInt(masks[2]) << 8) | Integer.parseInt(masks[3]);
        String[] networks = network.split("\\.");
        int net = (Integer.parseInt(networks[0]) << 24) | (Integer.parseInt(networks[1]) << 16) | (Integer.parseInt(networks[2]) << 8) | Integer.parseInt(networks[3]);
        return (ipAddr & mask) == net;
    }
}
