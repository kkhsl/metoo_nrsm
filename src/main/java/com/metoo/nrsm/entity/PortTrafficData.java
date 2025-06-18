package com.metoo.nrsm.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "port_traffic_data")
public class PortTrafficData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "port_index", length = 50)
    private String portIndex;

    @Column(name = "port_name", length = 255)
    private String portName;

    @Column(name = "is_ipv6")
    private Boolean isIpv6;

    @Column(name = "timestamp")
    private Long timestamp;

    @Column(name = "in_bytes", precision = 30, scale = 0)
    private BigDecimal inBytes;

    @Column(name = "out_bytes", precision = 30, scale = 0)
    private BigDecimal outBytes;

    @Column(name = "in_delta", precision = 30, scale = 0)
    private BigDecimal inDelta;

    @Column(name = "out_delta", precision = 30, scale = 0)
    private BigDecimal outDelta;

}