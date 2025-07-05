package com.metoo.nrsm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Route6Entry {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;
    private String deviceIp;
    private String destnetwork;
    private String mask;

    @JsonProperty("interface")
    private String interfaceName;

    private String port;

    private String nexthop;
    private Integer cost;
    private String type;
    private String preference;
}