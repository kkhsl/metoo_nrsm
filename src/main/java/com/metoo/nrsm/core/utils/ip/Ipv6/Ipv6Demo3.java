package com.metoo.nrsm.core.utils.ip.Ipv6;


import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-25 15:32
 */
public class Ipv6Demo3 {

    public static void main(String[] args) throws Exception {
        String ipv6 = "2001:db8:0:1234::"; // 示例IPv6地址
        int cidrMask = 48; // 示例掩码长度

        generateIpv6Network(InetAddress.getByName(ipv6), cidrMask);
    }

    public static void generateIpv6Network(InetAddress address, int cidrMask) {
        byte[] addressBytes = address.getAddress();
        BigInteger ipBigInt = new BigInteger(1, addressBytes);
        int prefixBits = addressBytes.length * 8 - cidrMask;
        BigInteger prefix = ipBigInt.shiftRight(prefixBits); // 掩码对应的前缀
        BigInteger mask = BigInteger.ONE.shiftLeft(cidrMask).subtract(BigInteger.ONE); // 掩码对应的掩码值

        String networkAddress = prefix.and(mask).toString(16); // 网段起始地址
        String broadcastAddress = prefix.and(mask).add(mask.not()).toString(16); // 网段结束地址

        System.out.println("Network Address: " + networkAddress);
        System.out.println("Broadcast Address: " + broadcastAddress);
    }

}
