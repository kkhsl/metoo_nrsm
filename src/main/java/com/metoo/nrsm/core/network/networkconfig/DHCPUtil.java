package com.metoo.nrsm.core.network.networkconfig;

import com.metoo.nrsm.core.network.networkconfig.test.*;
import com.metoo.nrsm.core.network.networkconfig.other.*;
import com.metoo.nrsm.core.utils.gather.thread.GatherDataThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DHCPUtil {


    private final PingTest pingTest;
    @Autowired
    public DHCPUtil(PingTest pingTest) {
        this.pingTest = pingTest;
    }


    /**
     * 获取dhcp状态
     * getdhcp.py
     *
     * @return {"v6int":"","v4int":"","v6status":"true","v4status":"true"}
     */
    public static String getDhcpStatus() {
        return getDhcp.getDhcpStatus();
    }

    /**
     * 获取dhcp进程状态
     *
     * @return
     */
    public static String checkdhcpd(String type) {
        return checkProcessStatus.checkProcessStatus(type);
    }

    // 重启dhcpd或dhcpd6
    public static String processOperation(String operation, String service) {
        return dhcpDop.processOp(operation, service);
    }


    // 重启保存 DHCP 接口配置 dhcpd或dhcpd6
    public static void modifyDHCP(String v4status, String v4int,
                                  String v6status, String v6int) {
        try {
            ModifyDhcp.dhcpsave(v4status, v4int, v6status, v6int);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取dns
    public static String getDnsSettings() {
        return getDns.getDnsSettings();
    }

    //modifydns.py
    public static void modifyDNS(String dns1, String dns2) {
        modifyDns.changeDNS(dns1,dns2);
    }

    //getnetintf.py
    public static String getNetworkInterfaces() {
        try {
            return getNetIntf.getNetworkInterfaces();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //modifyip.py
    public static int modifyIp(String iface, String ipv4address, String ipv6address,
                               String gateway4, String gateway6) {
        try {
            return modifyIp.modifyIP(iface,ipv4address,ipv6address,gateway4,gateway6);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //pingop.py
    public static String pingOp(String action, String service) {
        return pingOp.executeSystemctl(action,service);
    }

    //PingTest.py
//    public void pingSubnet(String network, int mask) {
//        pingTest.scanSubnet(network,mask);
//    }

    public static void pingSubnet(String network, int mask) {
        PingSubnet.scanSubnet(network,mask);
    }

    public void pingSubnet2(String network, int mask) {
        PingSubnet.scanSubnet(network,mask);
    }


}
