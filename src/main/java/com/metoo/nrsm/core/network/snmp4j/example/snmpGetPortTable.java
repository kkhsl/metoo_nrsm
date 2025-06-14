package com.metoo.nrsm.core.network.snmp4j.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import org.json.JSONArray;

public class snmpGetPortTable {

    public static void main(String[] args) throws JsonProcessingException {

        String host = "192.168.6.1";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "public@123";  // SNMP 社区字符串
        SNMPParams snmpParams = new SNMPParams(host, version, community);
        // 处理数据并返回结果
        JSONArray result = SNMPv2Request.getPortTableV6(snmpParams);
        System.out.println(result.toString());
    }

}