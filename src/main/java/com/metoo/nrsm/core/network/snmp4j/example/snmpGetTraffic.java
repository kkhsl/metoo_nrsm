package com.metoo.nrsm.core.network.snmp4j.example;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;

public class snmpGetTraffic {

    public static void main(String[] args) {
        String host = "192.168.6.1";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "public@123";  // SNMP 社区字符串
        String oid1 = "1.3.6.1.2.1.2.2.1.10.195";
        String oid2 = "1.3.6.1.2.1.2.2.1.10.195";
        SNMPParams snmpParams = new SNMPParams(host, version, community);
        // 处理数据并返回结果
        String traffic = SNMPv2Request.getTraffic(snmpParams, oid1, oid2);
        System.out.println(traffic.toString());
    }


}