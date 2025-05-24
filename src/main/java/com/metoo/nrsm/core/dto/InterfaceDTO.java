package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Interface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-22 10:20
 */
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class InterfaceDTO extends PageDto<Interface> {

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

}
