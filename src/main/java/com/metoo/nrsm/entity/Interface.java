package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-26 11:27
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Interface extends IdEntity {

    private String isup;
    private String ipv4netmask;
    private String ipv6netmask;
    private String macaddress;


    // 子接口 列表
    private List<Interface> vlans = new ArrayList<>(); // 新增字段



    private String name;        // 接口名称，例如 eth0, eth1
    private String ipv4Address; // IPv4 地址，例如 192.168.1.1/24
    private String ipv6Address; // IPv6 地址，例如 fc00::/64
    private String gateway4;    // IPv4 网关，例如 192.168.1.1
    private String gateway6;    // IPv6 网关，例如 fc00::1
    private Long parentId;      // 父接口 ID，如果为空则表示该接口是主接口
    private String parentName;  // 父接口 名称
    private Integer vlanNum;    // VLAN 配置编号


    private String ipv4NetworkSegment;  // 存储 IPv4 网段，如 "192.168.6.0/24"
    private String ipv6NetworkSegment;  // 存储 IPv6 网段，如 "fc00:1000:0:1::/64"



    // 用于构建树形结构的方法
    public void addChild(Interface child) {
        this.vlans.add(child);
    }

}
