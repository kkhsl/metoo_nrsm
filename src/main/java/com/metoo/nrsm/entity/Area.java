package com.metoo.nrsm.entity;

import lombok.Data;

import java.util.List;

@Data
public class Area {
    private Integer id;
    private String name;
    private String code;
    private String parentCode;
    private Integer level;

    private List<Area> children;
}