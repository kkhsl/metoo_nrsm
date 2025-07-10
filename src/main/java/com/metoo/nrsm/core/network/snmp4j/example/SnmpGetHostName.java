package com.metoo.nrsm.core.network.snmp4j.example;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;

public class SnmpGetHostName {

    // 测试通过snmp获取
    public static void main(String[] args) {
        String host = "192.168.0.1";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "transfar@123";  // SNMP 社区字符串
        SNMPParams snmpParams = new SNMPParams(host, version, community);
        System.out.println(SNMPv2Request.getDeviceArpPort(snmpParams));
    }
}
