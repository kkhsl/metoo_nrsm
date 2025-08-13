package com.metoo.nrsm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitHourFlowStats {

    private Long id;
    private Long unit_id;
    private String unit_name;
    private Double ipv4;
    private Double ipv6;
    private Integer hour_time;
    private Integer day;
    private Integer month;
    private Integer year;
    private Date stats_time;
}
