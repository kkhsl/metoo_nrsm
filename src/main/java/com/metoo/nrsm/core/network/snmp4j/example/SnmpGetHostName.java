package com.metoo.nrsm.core.network.snmp4j.example;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPRequest;

// 测试通过snmp获取
public class SnmpGetHostName {

    public static void main(String[] args) {
        String host = "192.168.6.1";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "public@123";  // SNMP 社区字符串
        SNMPParams snmpParams = new SNMPParams(host,version,community);
        String deviceName = SNMPRequest.getDeviceName(snmpParams);
        System.out.println(deviceName);
    }
}

/*
getDeviceArp
{"192.168.6.117":"00:e0:4c:68:18:2a","192.168.6.205":"60:ee:5c:35:c9:8f","192.168.6.129":"f8:4d:89:60:b4:da","192.168.6.1":"00:01:7a:94:a5:60","192.168.4.1":"58:48:49:29:bc:18","192.168.4.2":"00:01:7a:94:a5:60","192.168.5.1":"00:01:7a:94:a5:60","192.168.6.23":"b2:06:d0:d5:3f:1b","192.168.6.89":"96:63:36:05:57:87","192.168.6.100":"46:eb:f2:fe:44:1a","192.168.6.166":"18:31:bf:0c:7c:5a","192.168.6.101":"58:48:49:2f:f8:3e","192.168.6.189":"94:b8:6d:62:7d:16","192.168.6.252":"58:41:20:86:00:c0","192.168.6.253":"74:05:a5:2c:38:0d","192.168.6.11":"00:e0:4c:69:66:36","192.168.6.77":"50:33:f0:a4:33:c4","192.168.6.99":"ea:3c:b5:1d:9b:61","192.168.6.65":"00:25:b3:c8:69:e8","192.168.6.102":"30:9c:23:94:fb:e2","192.168.6.75":"60:fb:00:f4:34:7d"}

getDeviceArpPort
{"192.168.6.117":"193","192.168.6.205":"193","192.168.6.129":"193","192.168.6.1":"193","192.168.4.1":"195","192.168.5.1":"192","192.168.4.2":"195","192.168.6.23":"193","192.168.6.89":"193","192.168.6.100":"193","192.168.6.166":"193","192.168.6.101":"193","192.168.6.189":"193","192.168.6.252":"193","192.168.6.253":"193","192.168.6.11":"193","192.168.6.77":"193","192.168.6.99":"193","192.168.6.65":"193","192.168.6.75":"193","192.168.6.102":"193"}

SNMPRequest.getDevicePort
{"22":"gigabitethernet0/22","23":"gigabitethernet0/23","190":"null0","191":"vlan1","192":"vlan100","193":"vlan200","194":"vlan1234","195":"gigabitethernet0/24","10":"gigabitethernet0/10","11":"gigabitethernet0/11","12":"gigabitethernet0/12","13":"gigabitethernet0/13","14":"gigabitethernet0/14","15":"gigabitethernet0/15","16":"gigabitethernet0/16","17":"gigabitethernet0/17","18":"gigabitethernet0/18","19":"gigabitethernet0/19","1":"gigabitethernet0/1","2":"gigabitethernet0/2","3":"gigabitethernet0/3","4":"gigabitethernet0/4","5":"gigabitethernet0/5","6":"gigabitethernet0/6","7":"gigabitethernet0/7","8":"gigabitethernet0/8","9":"gigabitethernet0/9","20":"gigabitethernet0/20","21":"gigabitethernet0/21"}


SNMPRequest.getDevicePortStatus
{"22":"2","23":"2","190":"1","191":"2","192":"1","193":"1","194":"2","195":"1","10":"1","11":"1","12":"2","13":"2","14":"1","15":"2","16":"1","17":"2","18":"2","19":"2","1":"1","2":"2","3":"2","4":"2","5":"2","6":"2","7":"2","8":"2","9":"2","20":"1","21":"2"}

SNMPRequest.getDevicePortMac
{"22":"00:01:7A:94:A5:62","23":"00:01:7A:94:A5:62","190":"","191":"00:01:7A:94:A5:60","192":"00:01:7A:94:A5:60","193":"00:01:7A:94:A5:60","194":"00:01:7A:94:A5:60","195":"00:01:7A:94:A5:60","10":"00:01:7A:94:A5:62","11":"00:01:7A:94:A5:62","12":"00:01:7A:94:A5:62","13":"00:01:7A:94:A5:62","14":"00:01:7A:94:A5:62","15":"00:01:7A:94:A5:62","16":"00:01:7A:94:A5:62","17":"00:01:7A:94:A5:62","18":"00:01:7A:94:A5:62","19":"00:01:7A:94:A5:62","1":"00:01:7A:94:A5:62","2":"00:01:7A:94:A5:62","3":"00:01:7A:94:A5:62","4":"00:01:7A:94:A5:62","5":"00:01:7A:94:A5:62","6":"00:01:7A:94:A5:62","7":"00:01:7A:94:A5:62","8":"00:01:7A:94:A5:62","9":"00:01:7A:94:A5:62","20":"00:01:7A:94:A5:62","21":"00:01:7A:94:A5:62"}

[{"mac":"00:01:7A:94:A5:62","port":"gigabitethernet0/22", "status": "2"}]

SNMPRequest.getDevicePort
{"22":"gigabitethernet0/22","23":"gigabitethernet0/23","190":"null0","191":"vlan1","192":"vlan100","193":"vlan200","194":"vlan1234","195":"gigabitethernet0/24","10":"gigabitethernet0/10","11":"gigabitethernet0/11","12":"gigabitethernet0/12","13":"gigabitethernet0/13","14":"gigabitethernet0/14","15":"gigabitethernet0/15","16":"gigabitethernet0/16","17":"gigabitethernet0/17","18":"gigabitethernet0/18","19":"gigabitethernet0/19","1":"gigabitethernet0/1","2":"gigabitethernet0/2","3":"gigabitethernet0/3","4":"gigabitethernet0/4","5":"gigabitethernet0/5","6":"gigabitethernet0/6","7":"gigabitethernet0/7","8":"gigabitethernet0/8","9":"gigabitethernet0/9","20":"gigabitethernet0/20","21":"gigabitethernet0/21"}

SNMPRequest.getDevicePortIp
{"192.168.6.1":"193","192.168.4.2":"195","192.168.5.1":"192"}

SNMPRequest.getDevicePortMask
{"192.168.6.1":"255.255.255.0","192.168.4.2":"255.255.255.0","192.168.5.1":"255.255.255.0"}

SNMPRequest.getDevicePortDescription
{"22":"","23":"","190":"","191":"","192":"","193":"","194":"","195":"","10":"","11":"","12":"","13":"","14":"","15":"","16":"","17":"","18":"","19":"","1":"","2":"","3":"","4":"","5":"","6":"","7":"","8":"","9":"","20":"","21":""}


SNMPRequest.getDevicePort
{"52":"GigabitEthernet1/0/52","12":"GigabitEthernet1/0/12"}

SNMPRequest.getDeviceMac
{"43:D7:EB:B8:10":"12","0B:5F:E4:D6:18":"52"}

SNMPRequest.getDeviceMacType
{"0B:5F:E4:D6:18":"3","43:D7:EB:B8:10":"3"}

[{"mac":"00:0b:5f:e4:d6:18","port":"GigabitEthernet1/0/52","type":"3"}]




 */