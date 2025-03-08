package com.metoo.nrsm.core.network.snmp4j.constants;

public enum SNMP_OID {

    HOST_NAME("1.3.6.1.2.1.1.5.0"),   // 主机名 OID
    ARP("1.3.6.1.2.1.4.22.1.2"),   // arp
    ARP_PORT("1.3.6.1.2.1.4.22.1.1"),  // arp_port
    ARP_V6_PORT("1.3.6.1.2.1.4.22.1.1"),
    ARP_V6("1.3.6.1.2.1.55.1.12.1.2"),
    PORT("1.3.6.1.2.1.2.2.1.2"),
    PORT_IP("1.3.6.1.2.1.4.20.1.2"),
    PORT_IPV6("1.3.6.1.2.1.55.1.8.1.2"),
    PORT_MASK("1.3.6.1.2.1.4.20.1.3"),
    PORT_DESCRIPTION("1.3.6.1.2.1.31.1.1.1.18"),
    MAC("1.3.6.1.2.1.17.4.3.1.2"),
    MAC2("1.3.6.1.4.1.2011.5.25.42.2.1.3.1.4 -Cc"),
    PORT_STATUS("1.3.6.1.2.1.2.2.1.8"),
    PORT_MAC("1.3.6.1.2.1.2.2.1.6"),
    MAC_TYPE("1.3.6.1.2.1.17.4.3.1.3"),
    UP_TIME("1.3.6.1.2.1.1.3.0"),
    IPV6_DEVICE("1.3.6.1.2.1.55.1.1"),  // arp v6 port
    LLDP("1.0.8802.1.1.2.1.4.1.1.9"),
    LLDP_ROMOTE_PORT("1.0.8802.1.1.2.1.4.1.1.7"),
    IS_IPV6("1.3.6.1.2.1.55.1.1");

    private String oid;

    // 构造函数
    SNMP_OID(String oid) {
        this.oid = oid;
    }

    // 获取 OID 字符串
    public String getOid() {
        return this.oid;
    }
}
