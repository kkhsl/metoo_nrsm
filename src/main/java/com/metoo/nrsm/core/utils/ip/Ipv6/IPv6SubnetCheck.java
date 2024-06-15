package com.metoo.nrsm.core.utils.ip.Ipv6;

import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.entity.User;
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

    public static boolean isInSubnet(String ipv6Address, String subnetCidr) throws UnknownHostException {

        if(Ipv6Util.verifyIpv6(ipv6Address)){
            return false;
        }
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
        System.out.println(networkAddressString);
        System.out.println(subnetCidr.split("/")[0]);
        System.out.println();
        String a = TransIPv6.getShortIPv6(subnetCidr.split("/")[0]);
        return TransIPv6.getShortIPv6(networkAddressString).startsWith(subnetCidr.split("/")[0]);
    }

    public static void main(String[] args) {
        try {
            String ipv6Address = "240e:380:11d:2::a001";
            String subnetCidr = "240e:380:11d:2::/64";
            boolean isInSubnet = isInSubnet(ipv6Address, subnetCidr);
            System.out.println("IPv6 address " + ipv6Address + " is in subnet " + subnetCidr + ": " + isInSubnet);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test(){
        User a = new User();
        a.setUsername("a");

        User b = new User();

        BeanUtils.copyProperties(a, b);
        System.out.println(b);
    }

}
