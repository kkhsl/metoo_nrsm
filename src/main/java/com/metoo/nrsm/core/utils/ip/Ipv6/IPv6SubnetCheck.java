package com.metoo.nrsm.core.utils.ip.Ipv6;

import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.entity.User;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-15 14:15
 */
public class IPv6SubnetCheck {

    public static void main(String[] args) {
        String ipAddress = "240b:8150:3008:a00:7fac:2d8d:1fe1:3e3d";
        String network = "240B:8150:3008:0A00::0/64";

        boolean isInNetwork = isInSubnet(ipAddress, network);
        System.out.println("Is the IP address in the network? " + isInNetwork);
    }

    public static boolean isInSubnet(String ipAddress, String network) {
        try {
            String[] parts = network.split("/");
            InetAddress ip = InetAddress.getByName(ipAddress);
            InetAddress net = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] ipBytes = ip.getAddress();
            byte[] netBytes = net.getAddress();

            // Check if the prefix length is valid
            if (prefixLength < 0 || prefixLength > 128) {
                throw new IllegalArgumentException("Invalid prefix length");
            }

            // Compare the relevant bits
            int byteCount = prefixLength / 8;
            int bitCount = prefixLength % 8;

            // Compare full bytes
            for (int i = 0; i < byteCount; i++) {
                if (ipBytes[i] != netBytes[i]) {
                    return false;
                }
            }

            // Compare the remaining bits
            if (bitCount > 0) {
                int mask = 0xFF00 >> bitCount;
                if ((ipBytes[byteCount] & mask) != (netBytes[byteCount] & mask)) {
                    return false;
                }
            }

            return true;

        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }



}
