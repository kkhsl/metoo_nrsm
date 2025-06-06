package com.metoo.nrsm.core.network.snmp4j.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import org.json.JSONArray;

public class snmpGetPortTableIpv6 {

    public static void main(String[] args) throws JsonProcessingException {

        String host = "192.168.6.249";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "public@123";  // SNMP 社区字符串
        SNMPParams snmpParams = new SNMPParams(host, version, community);
        // 处理数据并返回结果
        JSONArray result = SNMPv2Request.getPortTableV6(snmpParams);
        System.out.println(result.toString());
    }

}

/*
[{"port":"Vlan-interface200","ipv6":"fc00:1000:0:1::1/64"},{"port":"Vlan-interface300","ipv6":"fc00:1000:0:100::1/64"},{"port":"Vlan-interface10","ipv6":"fc00:1000:0:10::1/64"},{"port":"Vlan-interface20","ipv6":"fc00:1000:0:20::1/64"},{"port":"Vlan-interface100","ipv6":"fc00:1000:0:50::1/64"},{"port":"GigabitEthernet1/0/47","ipv6":"fc00::2/126"}]


[{"port":"Vlan-interface200","ipv6":"fc00:1000:0:1::1/64"},{"port":"Vlan-interface300","ipv6":"fc00:1000:0:100::1/64"},{"port":"Vlan-interface10","ipv6":"fe80::72ba:efff:fe6a:a51d/10"},{"port":"GigabitEthernet1/0/47","ipv6":"fe80::72ba:efff:fe6a:a502/10"},{"port":"Vlan-interface10","ipv6":"fc00:1000:0:10::1/64"},{"port":"Vlan-interface20","ipv6":"fc00:1000:0:20::1/64"},{"port":"Vlan-interface200","ipv6":"fe80::72ba:efff:fe6a:a51b/10"},{"port":"Vlan-interface300","ipv6":"fe80::72ba:efff:fe6a:a51f/10"},{"port":"Vlan-interface20","ipv6":"fe80::72ba:efff:fe6a:a527/10"},{"port":"Vlan-interface100","ipv6":"fe80::72ba:efff:fe6a:a517/10"},{"port":"Vlan-interface100","ipv6":"fc00:1000:0:50::1/64"},{"port":"GigabitEthernet1/0/47","ipv6":"fc00::2/126"}]



 */