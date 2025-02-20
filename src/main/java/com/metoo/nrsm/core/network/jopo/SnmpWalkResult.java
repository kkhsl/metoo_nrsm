package com.metoo.nrsm.core.network.jopo;

public class SnmpWalkResult {

    private String version;

    private String community;

    private String ip;

    private String result;
//      private AtomicReference<String> result = new AtomicReference<>();

    // 设置 SNMP version
    public SnmpWalkResult setVersion(String version) {
        this.version = version;
        return this; // 返回当前对象，以便继续链式调用
    }

    // 设置 SNMP community
    public SnmpWalkResult setCommunity(String community) {
        this.community = community;
        return this;
    }

    // 设置目标 IP 地址
    public SnmpWalkResult setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public String getCommunity() {
        return community;
    }

    public String getIp() {
        return ip;
    }

    public synchronized String getResult() {
        return result;
    }

    public synchronized void setResult(String result) {
        this.result = result;
    }
}
