package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitSubnet {
    private Long id;
    private Date addTime;
    private String name;
    private String ipv4;
    private String ipv6;
    private int vlan;


}