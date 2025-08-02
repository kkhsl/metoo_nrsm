package com.metoo.nrsm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WebSet {

    private Long id;

    private String name;

    private String logoUrl;

    private String icoUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}