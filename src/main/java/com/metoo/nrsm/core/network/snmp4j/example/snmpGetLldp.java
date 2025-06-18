package com.metoo.nrsm.core.network.snmp4j.example;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import org.json.JSONArray;

public class snmpGetLldp {

    public static void main(String[] args) {
        String host = "192.168.6.1";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "public@123";  // SNMP 社区字符串
        SNMPParams snmpParams = new SNMPParams(host, version, community);
        // 处理数据并返回结果
        JSONArray result = SNMPv2Request.getLldp(snmpParams);
        System.out.println(result.toString());
    }


    /*


    SNMPv2Request.getLLDP
    {"8359.52.1":"2F_CF_HJ_7603_SW1"}

   SNMPv2Request.getLLDPPort
   {"256586179.43.1":"B888E3EB40AE","256479241.35.1":"28D2440F08DA","8359.52.1":"GigabitEthernet3/0/4"}


[{"hostname": "2F_CF_HJ_7603_SW1", "remoteport": "GigabitEthernet3/0/4"}]



     */

}