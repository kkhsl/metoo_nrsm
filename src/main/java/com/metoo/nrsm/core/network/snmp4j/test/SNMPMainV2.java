package com.metoo.nrsm.core.network.snmp4j.test;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SNMPMainV2 {

    @Test
    public void getDeviceName() {
        String host = "192.168.6.1";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "public@123";  // SNMP 社区字符串
        SNMPParams snmpParams = new SNMPParams(host, version, community);
        // 处理数据并返回结果
        String deviceName = SNMPv2Request.getDeviceName(snmpParams);
        log.info("设备名{}", deviceName);
    }
}
