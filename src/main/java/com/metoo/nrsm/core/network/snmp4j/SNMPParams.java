package com.metoo.nrsm.core.network.snmp4j;

public class SNMPParams {

    private String ip;           // IP 地址
    private String version;      // SNMP 版本
    private String community;    // SNMP 社区字符串

    // 构造函数
    public SNMPParams(String ip, String version, String community) {
        this.ip = ip;
        this.version = version;
        this.community = community;
    }

    // 获取 IP 地址
    public String getIp() {
        return ip;
    }

    // 获取 SNMP 版本
    public String getVersion() {
        return version;
    }

    // 获取社区字符串
    public String getCommunity() {
        return community;
    }

    // 设置 IP 地址
    public void setIp(String ip) {
        this.ip = ip;
    }

    // 设置 SNMP 版本
    public void setVersion(String version) {
        this.version = version;
    }

    // 设置社区字符串
    public void setCommunity(String community) {
        this.community = community;
    }

    // 用于打印参数的辅助方法
    @Override
    public String toString() {
        return "SNMPParams{" +
                "ip='" + ip + '\'' +
                ", version='" + version + '\'' +
                ", community='" + community + '\'' +
                '}';
    }
}
