package com.metoo.nrsm.core.utils.ip;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-13 9:41
 */
public class CIDRUtils {
    private final InetAddress inetAddress;
    private final InetAddress startAddress;
    private final InetAddress endAddress;
    private final int prefixLength;

    public CIDRUtils(String cidr) throws UnknownHostException {
        int index = cidr.indexOf("/");
        String addressPart = cidr.substring(0, index);
        String networkPart = cidr.substring(index + 1);

        inetAddress = InetAddress.getByName(addressPart);
        prefixLength = Integer.parseInt(networkPart);

        // Calculate the start and end addresses
        int targetSize = inetAddress.getAddress().length;
        BigInteger mask = BigInteger.ONE.shiftLeft(targetSize * 8).subtract(BigInteger.ONE)
                .shiftRight(prefixLength)
                .shiftLeft(prefixLength);

        BigInteger ipVal = new BigInteger(1, inetAddress.getAddress());
        BigInteger startIp = ipVal.and(mask);
        BigInteger endIp = startIp.add(mask.not());

        byte[] startIpArr = toBytes(startIp, targetSize);
        byte[] endIpArr = toBytes(endIp, targetSize);

        startAddress = InetAddress.getByAddress(startIpArr);
        endAddress = InetAddress.getByAddress(endIpArr);
    }

    private byte[] toBytes(BigInteger val, int size) {
        byte[] byteArray = val.toByteArray();
        int byteArrayLength = byteArray.length;
        byte[] result = new byte[size];

        int start = byteArrayLength > size ? byteArrayLength - size : 0;
        int length = Math.min(byteArrayLength, size);

        System.arraycopy(byteArray, start, result, size - length, length);
        return result;
    }

    public boolean isInRange(String ip) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(ip);
        BigInteger start = new BigInteger(1, startAddress.getAddress());
        BigInteger end = new BigInteger(1, endAddress.getAddress());
        BigInteger target = new BigInteger(1, address.getAddress());

        return target.compareTo(start) >= 0 && target.compareTo(end) <= 0;
    }

    public static void main(String[] args) throws UnknownHostException {
        CIDRUtils cidrUtils = new CIDRUtils("2001:db8::/32");

        String testIp = "2001:db8::1";
        System.out.println("Is IP " + testIp + " in range? " + cidrUtils.isInRange(testIp));
    }
}
