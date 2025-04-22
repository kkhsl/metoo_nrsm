package com.metoo.nrsm.core.network.snmp4j.example;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.Test;

@Slf4j
public class snmpGetMac {

    public static void main(String[] args) {
        String host = "192.168.0.1";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "transfar@123";  // SNMP 社区字符串
        SNMPParams snmpParams = new SNMPParams(host, version, community);
        // 处理数据并返回结果
        JSONArray result = SNMPv2Request.getMac(snmpParams);
        System.out.println(result.toString());
    }



    @Test
    public void getArpV6() {
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.0.40")
                .port(161)
                .community("transfar@123")
                .build();

        JSONArray result = SNMPv3Request.getMac(snmpv3Params);
        log.info("arpV6:{}", result);

    }




}