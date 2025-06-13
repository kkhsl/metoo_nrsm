package com.metoo.nrsm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConfig {
    private Long id;
    private String name;
    private Date time;
    private Integer type;
    private String content;
}