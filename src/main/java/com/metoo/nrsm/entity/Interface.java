package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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

    private String name;
    private String isup;
    private String ipv4address;
    private String ipv4netmask;
    private String ipv6address;
    private String ipv6netmask;
    private String macaddress;
    private String gateway4;
    private String gateway6;

    // 子接口 列表
    private List<Vlans> vlans; // 新增字段

}
