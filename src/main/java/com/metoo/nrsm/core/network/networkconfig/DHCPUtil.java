package com.metoo.nrsm.core.network.networkconfig;

import com.metoo.nrsm.core.network.networkconfig.test.ModifyDhcp;
import com.metoo.nrsm.core.network.networkconfig.test.checkProcessStatus;
import com.metoo.nrsm.core.network.networkconfig.test.dhcpDop;
import com.metoo.nrsm.core.network.networkconfig.test.getDhcp;

import java.io.IOException;

public class DHCPUtil {

    /**
     * 获取dhcp状态
     * getdhcp.py
     * @return  {"v6int":"","v4int":"","v6status":"true","v4status":"true"}
     */
    public static String getDhcpStatus() {
        return getDhcp.getDhcpStatus();
    }

    /**
     * 获取dhcp进程状态
     * @return
     */
    public static String checkdhcpd(String type){
        return checkProcessStatus.checkProcessStatus(type);
    }

    // 重启dhcpd或dhcpd6
    public static String processOperation(String operation, String service){
        return dhcpDop.processOp(operation, service);
    }


    // 重启dhcpd或dhcpd6
    public static void modifyDHCP(String v4status, String v4int,
                                    String v6status, String v6int){
        try {
            ModifyDhcp.dhcpsave(v4status, v4int, v6status, v6int);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
