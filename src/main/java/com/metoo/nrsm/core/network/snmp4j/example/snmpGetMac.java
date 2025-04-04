package com.metoo.nrsm.core.network.snmp4j.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPRequest;
import org.json.JSONArray;

import java.util.*;

public class snmpGetMac {

    public static void main(String[] args) {
        String host = "192.168.0.1";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "transfar@123";  // SNMP 社区字符串
        SNMPParams snmpParams = new SNMPParams(host, version, community);
        // 处理数据并返回结果
        JSONArray result = SNMPRequest.getMac(snmpParams);
        System.out.println(result.toString());
    }

}