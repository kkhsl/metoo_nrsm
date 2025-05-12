package com.metoo.nrsm.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Vlans {
    // VLAN ID
    private String id;

    private String isup;

    // IPv4 地址（含掩码）
    private String ipv4address;
    // IPv6 地址（含掩码）
    private String ipv6address;
    // IPv4 网关
    private String gateway4;
    // IPv6 网关
    private String gateway6;
}