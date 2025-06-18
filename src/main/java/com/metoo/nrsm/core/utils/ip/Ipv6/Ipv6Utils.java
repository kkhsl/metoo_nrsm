package com.metoo.nrsm.core.utils.ip.Ipv6;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-25 14:56
 */
public class Ipv6Utils {


    public static void main(String[] args) throws UnknownHostException {
        String ipv6 = "240e:381:11d:4100::b";

        String mask = "64";

        System.out.println(ipRange(ipv6, mask));;

    }


    //ipv6恢复还原
    public static String ipRepair(String ip) {
        //ip 还原
        System.out.println(String.format("原ip:%s",ip));
        int index = ip.indexOf("::");
        String tmp = "";
        if(index>0&&index==ip.length()-2){
            int size = 8 - (ip.split(":").length);
            for (int j = 0; j < size; j++) {
                tmp += ":0000";
            }
        }else if(index == 0&&"::".equalsIgnoreCase(ip))
            tmp = "0000:0000:0000:0000:0000:0000:0000:0000";
        else {
            int size = 8 - (ip.split(":").length-1);
            for (int j = 0; j < size; j++) {
                tmp += ":0000";
            }
            tmp+=":";
        }
        ip = ip.replace("::", tmp);
        System.out.println(String.format("展开ip:%s", ip));
        String[] split = ip.split(":");
        for (int i = 0; i < split.length; i++) {
            split[i]= Collections.nCopies(4-split[i].length(),"0").stream().collect(Collectors.joining(""))+split[i];
        }
        return String.join(":",split);
    }

    //携带掩码位数ip获取ip网段范围(16进制)
    public static String[] ipRange(String ip,String mask){
        //掩码计算
        ip = ipRepair(ip);
        System.out.println("修复:"+ip);
        int maskNum=Integer.parseInt(mask);
        System.out.println("掩码:"+maskNum);
        int zeroNum=((128-maskNum)/16)*4+(128-maskNum)%16/4;
        int changeNum=(128-maskNum)%16%4==0?0:1;
        System.out.println("改变:"+changeNum);
        int copyNum=32-zeroNum-changeNum;
        if (copyNum==32) return new String[]{ip,ip};
        String minIP=String.format("%s%s%s",ip.substring(0,copyNum/4+copyNum)
                ,changeNum==0?"":ip.substring(copyNum/4+copyNum,copyNum/4+copyNum+changeNum)
                ,ip.substring(copyNum/4+copyNum+changeNum).toLowerCase().replaceAll("[0-9|a-z]","0"));
        String maxIP=String.format("%s%s%s",ip.substring(0,copyNum/4+copyNum)
                ,changeNum==0?"":Integer.toHexString(Integer.parseInt(ip.substring(copyNum/4+copyNum
                        ,copyNum/4+copyNum+changeNum),16)+(2<<(128-maskNum)%16%4-1)-1)
                ,ip.substring(copyNum/4+copyNum+changeNum).toLowerCase().replaceAll("[0-9|a-z]","f"));
        return new String[]{minIP, maxIP};
    }
    //携带掩码位数ip获取ip网段范围(10进制)
    public String[] ipRange(String ip, String mask, int radix) throws UnknownHostException {
        String[] ipRange = ipRange(ip, mask);
        if(radix==10){
            ipRange[0]=String.valueOf(dot2LongIP(ipRange[0]));
            ipRange[1]=String.valueOf(dot2LongIP(ipRange[1]));
            return ipRange;
        }else throw new RuntimeException(String.format("radix %s not support", radix));
    }

    public static BigInteger dot2LongIP(String ip) throws UnknownHostException {
        InetAddress byName = InetAddress.getByName(ip);
        byte[] address = byName.getAddress();
        if(byName instanceof Inet6Address){
            BigInteger bigInteger = new BigInteger(1, address);
            return bigInteger;
        }else throw new RuntimeException(String.format("ip %s is not ipV6", ip));
    }


    public static String getIpv6networkAddress(String ipv6Address, String subnetCidr) throws UnknownHostException {
        // 将子网CIDR表示法转换为网络掩码
        int cidrLength = Integer.parseInt(subnetCidr.split("/")[1]);
        byte[] subnetMask = new byte[16];
        for (int i = 0; i < 16; i++) {
            subnetMask[i] = (byte) (cidrLength > i * 8 ? 0xFF : 0x00);
            if (cidrLength > i * 8 + 8) {
                subnetMask[i] |= 0xFF << (cidrLength - i * 8 - 8);
            }
        }

        // 获取IPv6地址和子网掩码
        InetAddress address = InetAddress.getByName(ipv6Address);
        InetAddress subnetMaskAddress = InetAddress.getByAddress(subnetMask);

        // 进行子网掩码操作以获取网络地址
        byte[] networkAddressBytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            networkAddressBytes[i] = (byte) ((address.getAddress()[i] & subnetMaskAddress.getAddress()[i]));
        }

        // 将网络地址转换回字符串并与子网CIDR前缀进行比较
        InetAddress networkAddress = InetAddress.getByAddress(networkAddressBytes);
        String networkAddressString = networkAddress.getHostAddress();
        return TransIPv6.getShortIPv6(networkAddressString);
    }


    // 判断ip地址是否属于网段

}
