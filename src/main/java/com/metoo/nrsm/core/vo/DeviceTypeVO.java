package com.metoo.nrsm.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTypeVO {

    private String name;
    private Integer count;
    private Integer online;
    private Integer overProtection;
}
