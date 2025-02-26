package com.metoo.nrsm.core.network.snmp4j.mockito;

public class SNMPClient {

    public String getHostname(String dhost, String version, String community, String oid) {
        // 模拟 SNMP 查询
        return "设备 " + dhost + " 的主机名";
    }

    public String getTraffic(String dhost, String version, String community, String oid) {
        // 模拟 SNMP 查询
        return "设备 " + dhost + " 的流量数据";
    }

    public String getIpv4Port(String dhost, String version, String community, String ip, String oid) {
        // 模拟 SNMP 查询
        return "设备 " + ip + " 的 IPv4 端口数据";
    }

    public String getIpv6Port(String dhost, String version, String community, String ip, String oid) {
        // 模拟 SNMP 查询
        return "设备 " + ip + " 的 IPv6 端口数据";
    }

    public String getIpv6PortHillstone(String dhost, String version, String community, String ip, String oid) {
        // 模拟 Hillstone 设备的 SNMP 查询
        return "Hillstone 设备 " + ip + " 的 IPv6 端口数据";
    }
}
