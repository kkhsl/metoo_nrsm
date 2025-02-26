package com.metoo.nrsm.core.network.snmp4j.test;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPRequest;

public class SNMPMain {

    public static void main(String[] args) {
        // 获取设备名
        SNMPParams snmpParams = new SNMPParams("192.168.6.1", "v2c", "public@123");
        String hostName = SNMPRequest.getDeviceName(snmpParams);
        System.out.println("主机名：" + hostName);

    }
}
