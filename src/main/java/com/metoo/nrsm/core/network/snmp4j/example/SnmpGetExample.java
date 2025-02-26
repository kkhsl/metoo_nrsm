package com.metoo.nrsm.core.network.snmp4j.example;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

// 测试通过snmp获取设备名
public class SnmpGetExample {

    public static String getHostName(String host, String version, String community) {
        String targetAddress = "udp:" + host + "/161"; // SNMP设备地址
        String oid = "1.3.6.1.2.1.1.5.0"; // OID for sysName (hostname)

        try {
            Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
            snmp.listen();

            // 目标地址
            Address target = GenericAddress.parse(targetAddress);

            CommunityTarget communityTarget = new CommunityTarget();

            communityTarget.setCommunity(new OctetString(community));

            // 根据版本选择 SNMP 版本
            if (version.equalsIgnoreCase("v1")) {
                communityTarget.setVersion(SnmpConstants.version1);  // SNMP v1
            } else if (version.equalsIgnoreCase("v2c")) {
                communityTarget.setVersion(SnmpConstants.version2c);  // SNMP v2c
            } else if (version.equalsIgnoreCase("v3")) {
                communityTarget.setVersion(SnmpConstants.version3);  // SNMP v3, but needs further configuration for security
            } else {
                throw new IllegalArgumentException("Unsupported SNMP version: " + version);
            }

            communityTarget.setAddress(target);
            communityTarget.setRetries(3);  // 增加重试次数
            communityTarget.setTimeout(3000);  // 增加超时时间到 3000 毫秒

            // 创建请求
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            // 发送请求并接收响应
            ResponseEvent response = snmp.send(pdu, communityTarget);
            if (response == null) {
                System.out.println("No response received.");
            } else {
                PDU responsePdu = response.getResponse();
                if (responsePdu == null) {
                    System.out.println("Response received, but no PDU.");
                } else {
                    // 获取主机名
                    String hostname = responsePdu.getVariableBindings().firstElement().toString();
                    hostname = hostname.split("=")[1].trim(); // 获取=后面的部分
                    hostname = hostname.replace("STRING: ", "").replace("\"", ""); // 清理字符串中的不必要字符
                    return hostname;
                }
            }
            snmp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        String host = "192.168.6.1";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "public@123";  // SNMP 社区字符串

        String hostname = getHostName(host, version, community);
        System.out.println("Hostname: " + hostname);
    }
}
