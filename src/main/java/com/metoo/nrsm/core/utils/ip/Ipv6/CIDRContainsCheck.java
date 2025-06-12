package com.metoo.nrsm.core.utils.ip.Ipv6;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddressString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class CIDRContainsCheck {

    public static void main(String[] args) {
        String cidr1 = "192.168.6.104/23";
        String cidr2 = "192.168.6.101/24";

        IPAddressString addr1 = new IPAddressString(cidr1);
        IPAddressString addr2 = new IPAddressString(cidr2);

        // 转换为网络地址（192.168.0.0/24 和 192.168.0.0/25）
        log.info("192.168.0.1/24 网络地址：{}", addr1.getAddress().toPrefixBlock());
        log.info("192.168.0.3/25 网络地址：{}", addr2.getAddress().toPrefixBlock());


        boolean contains = addr1.getAddress().toPrefixBlock().contains(addr2.getAddress().toPrefixBlock());

        System.out.println(addr1 + " contains " + addr2 + "? " + contains);  // true
    }

    /**
     * 判断地址1是否包含地址2
     * @param cidr1
     * @param cidr2
     * @return
     */
    public static boolean checkCIDRInclusion(String cidr1, String cidr2) {
        IPAddressString addr1 = new IPAddressString(cidr1);
        IPAddressString addr2 = new IPAddressString(cidr2);

        if (!addr1.isValid() || !addr2.isValid()) {
            return false;
        }

        // 必须同为IPv4或同为IPv6
        if (addr1.getAddress().isIPv4() != addr2.getAddress().isIPv4()) {
            return false;
        }

        return addr1.getAddress().toPrefixBlock().contains(addr2.getAddress().toPrefixBlock()) ||
                addr2.getAddress().toPrefixBlock().contains(addr1.getAddress().toPrefixBlock());
    }

    @Test
    public void test(){
        System.out.println(checkCIDRInclusion("192.168.0.3/25", "192.168.0.1/24"));

        // IPv6测试用例
        System.out.println(checkCIDRInclusion("2001:db8::/32", "2001:db8:abcd::/48")); // true (包含)
        System.out.println(checkCIDRInclusion("2001:db8:abcd::/48", "2001:db8::/32")); // true (被包含)
        System.out.println(checkCIDRInclusion("2001:db8::/64", "2001:db9::/64")); // false (无关系)
        System.out.println(checkCIDRInclusion("::1/128", "::1/128")); // true (相同)
        System.out.println(checkCIDRInclusion("invalid", "2001:db8::/32")); // false (无效)
        System.out.println(checkCIDRInclusion("192.168.1.0/24", "2001:db8::/32")); // false (IPv4与IPv6)
    }
}
