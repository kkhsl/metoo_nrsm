package com.metoo.nrsm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FlowSummary {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;         // 数据时间点
    private BigDecimal ipv4TotalGb;      // IPv4总流量(GB)
    private BigDecimal ipv6TotalGb;      // IPv6总流量(GB)
    private Double ipv6Percentage;       // IPv6流量占比(%)
}