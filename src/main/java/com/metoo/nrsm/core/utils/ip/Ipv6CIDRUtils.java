package com.metoo.nrsm.core.utils.ip;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-13 9:41
 */
public class Ipv6CIDRUtils {

    private final InetAddress inetAddress;
    private final InetAddress startAddress;
    private final InetAddress endAddress;
    private final int prefixLength;

    public Ipv6CIDRUtils(String cidr) throws UnknownHostException {
        int index = cidr.indexOf("/");
        if (index < 0) {
            throw new IllegalArgumentException("Invalid CIDR format");
        }
        String addressPart = cidr.substring(0, index);
        String networkPart = cidr.substring(index + 1);

        inetAddress = InetAddress.getByName(addressPart);
        prefixLength = Integer.parseInt(networkPart);

        startAddress = calculateStartAddress();
        endAddress = calculateEndAddress();
    }

    private InetAddress calculateStartAddress() throws UnknownHostException {
        int targetSize = inetAddress.getAddress().length;
        BigInteger mask = BigInteger.ONE.shiftLeft(targetSize * 8).subtract(BigInteger.ONE)
                .shiftRight(prefixLength)
                .shiftLeft(prefixLength);

        BigInteger ipVal = new BigInteger(1, inetAddress.getAddress());
        BigInteger startIp = ipVal.and(mask);

        byte[] startIpArr = toBytes(startIp, targetSize);
        return InetAddress.getByAddress(startIpArr);
    }

    private InetAddress calculateEndAddress() throws UnknownHostException {
        int targetSize = inetAddress.getAddress().length;
        BigInteger mask = BigInteger.ONE.shiftLeft(targetSize * 8).subtract(BigInteger.ONE)
                .shiftRight(prefixLength)
                .shiftLeft(prefixLength);

        BigInteger ipVal = new BigInteger(1, inetAddress.getAddress());
        BigInteger startIp = ipVal.and(mask);
        BigInteger endIp = startIp.add(mask.not());

        byte[] endIpArr = toBytes(endIp, targetSize);
        return InetAddress.getByAddress(endIpArr);
    }

    private byte[] toBytes(BigInteger val, int size) {
        byte[] byteArray = val.toByteArray();
        byte[] result = new byte[size];
        int srcPos = Math.max(0, byteArray.length - size);
        int destPos = Math.max(0, size - byteArray.length);
        int length = Math.min(size, byteArray.length);

        System.arraycopy(byteArray, srcPos, result, destPos, length);
        return result;
    }

    public boolean isInRange(String ip) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(ip);
        BigInteger start = new BigInteger(1, startAddress.getAddress());
        BigInteger end = new BigInteger(1, endAddress.getAddress());
        BigInteger target = new BigInteger(1, address.getAddress());

        return target.compareTo(start) >= 0 && target.compareTo(end) <= 0;
    }

    /**
     * 计算 IPv6 的网段（网络地址）
     * @param cidr 如 "fc00:1000:0:1::3/64"
     * @return 网段，如 "fc00:1000:0:1::/64"
     */
    public static String getIPv6NetworkSegment(String cidr) throws UnknownHostException {
        String[] parts = cidr.split("/");
        String ip = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        // 将 IPv6 地址转为字节数组（16字节）
        byte[] ipBytes = Inet6Address.getByName(ip).getAddress();

        // 计算网络地址（按前缀长度置零）
        for (int i = prefixLength / 8; i < 16; i++) {
            if (i == prefixLength / 8) {
                int bitsToKeep = prefixLength % 8;
                ipBytes[i] &= (0xFF << (8 - bitsToKeep));
            } else {
                ipBytes[i] = 0;
            }
        }

        // 返回网段
        String networkAddress = Inet6Address.getByAddress(ipBytes).getHostAddress();
        return networkAddress + "/" + prefixLength;
    }


    public static void main(String[] args) throws UnknownHostException {
        Ipv6CIDRUtils ipv6CIDRUtils = new Ipv6CIDRUtils("2001:db8::/32");

        String testIp = "2001:db8::1";
        System.out.println("Is IP " + testIp + " in range? " + ipv6CIDRUtils.isInRange(testIp));

        String outOfRangeIp = "2001:db9::1";
        System.out.println("Is IP " + outOfRangeIp + " in range? " + ipv6CIDRUtils.isInRange(outOfRangeIp));


        String cidr = "fc00:1000:0:1::3/64";
        String networkSegment = getIPv6NetworkSegment(cidr);
        System.out.println("IPv6 网段: " + networkSegment); // 输出: fc00:1000:0:1::/64
    }
}