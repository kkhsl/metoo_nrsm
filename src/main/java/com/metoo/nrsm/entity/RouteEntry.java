package com.metoo.nrsm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class RouteEntry {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;
    private String deviceIp;
    private String destnetwork;
    private String mask;

    @JsonProperty("interface")
    private String interfaceName;

    private String nexthop;
    private Integer cost;
    private String type;

    // 默认构造函数
    public RouteEntry() {}

    // 完整构造函数
    public RouteEntry(Date time, String deviceIp, String destnetwork, String mask,
                      String interfaceName, String nexthop, Integer cost, String type) {
        this.time = time;
        this.deviceIp = deviceIp;
        this.destnetwork = destnetwork;
        this.mask = mask;
        this.interfaceName = interfaceName;
        this.nexthop = nexthop;
        this.cost = cost;
        this.type = type;
    }

    // Getters and Setters
    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }

    public String getDeviceIp() { return deviceIp; }
    public void setDeviceIp(String deviceIp) { this.deviceIp = deviceIp; }

    public String getDestnetwork() { return destnetwork; }
    public void setDestnetwork(String destnetwork) { this.destnetwork = destnetwork; }

    public String getMask() { return mask; }
    public void setMask(String mask) { this.mask = mask; }

    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public String getNexthop() { return nexthop; }
    public void setNexthop(String nexthop) { this.nexthop = nexthop; }

    public Integer getCost() { return cost; }
    public void setCost(Integer cost) { this.cost = cost; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "RouteEntry{" +
                "time=" + time +
                ", deviceIp='" + deviceIp + '\'' +
                ", destnetwork='" + destnetwork + '\'' +
                ", mask='" + mask + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", nexthop='" + nexthop + '\'' +
                ", cost=" + cost +
                ", type='" + type + '\'' +
                '}';
    }
}