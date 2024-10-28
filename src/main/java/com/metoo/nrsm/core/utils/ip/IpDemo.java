package com.metoo.nrsm.core.utils.ip;

import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-23 10:42
 */
public class IpDemo {

//    @TestDeCorator
//    public void test4(){
//        String ip = "192.168.5.2-192.168.5.100";
//        String a = ip.substring(0, ip.indexOf("-"));
//        String b = ip.substring(ip.indexOf("-") +1);
//        int i = compareIP(a, b);
//        System.out.println(i);
//
//        int ii = compareIP(b, a);
//        System.out.println(ii);
//
//    }
//
//    public int compareIP(String ip1, String ip2) {
//        BigInteger ipValue1 = ipToBigInteger(ip1);
//        BigInteger ipValue2 = ipToBigInteger(ip2);
//
//        if (ipValue1 != null && ipValue2 != null) {
//            return ipValue1.compareTo(ipValue2);
//        }
//
//        return 0;
//    }
//
//    public static BigInteger ipToBigInteger(String ipAddress) {
//        try {
//            InetAddress inetAddress = InetAddress.getByName(ipAddress);
//            byte[] bytes = inetAddress.getAddress();
//            return new BigInteger(1, bytes);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }





//    public static void main(String[] args) throws UnknownHostException {
//        String ip = "192.168.5.1"; // 要判断的IP地址
//        String subnetMask2 = "255.255.255.0"; // 子网掩码
//
//        String subnetMask = IpV4Util.bitMaskConvertMask(23);
//
//        InetAddress inetAddr = InetAddress.getByName(ip);
//        byte[] addrBytes = inetAddr.getAddress();
//        byte[] maskBytes = getSubnetMaskBytes(subnetMask);
//
//        boolean isSameNetwork = true;
//        for (int i = 0; i < addrBytes.length && isSameNetwork; ++i) {
//            if ((addrBytes[i] & maskBytes[i]) != addrBytes[i]) {
//                isSameNetwork = false;
//            }
//        }
//
//        System.out.println("该IP地址" + (isSameNetwork ? "" : "不") + "属于指定的子网");
//    }
//
//    private static byte[] getSubnetMaskBytes(String subnetMask) throws UnknownHostException {
//        return InetAddress.getByName(subnetMask).getAddress();
//    }


    public static void main(String[] args) {
        String mask = Ipv4Util.getMaskByMaskBit(23);
        System.out.println(mask);
        String v = Ipv4Util.getNetwork("192.168.5.0", mask);
        System.out.println(v);

//        String networkAddress = IpV4Util.getNetworkAddress("192.168.5.0",  23);
//        System.out.println(networkAddress);

        Map<String, String> a = Ipv4Util.getNetworkIp("192.168.5.0",  mask);
        System.out.println(a.get("network"));

    }

}
