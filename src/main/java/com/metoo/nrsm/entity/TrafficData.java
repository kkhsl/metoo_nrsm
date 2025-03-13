package com.metoo.nrsm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrafficData {
    private Integer id;
    private Date addTime;
    private String ipv4InputRate;
    private String ipv4OutputRate;
    private String ipv6InputRate;
    private String ipv6OutputRate;
    private Integer vlanId;
}