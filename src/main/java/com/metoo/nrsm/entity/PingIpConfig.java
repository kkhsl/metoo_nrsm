package com.metoo.nrsm.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 16:24
 *
 * 保持表里默认有一条数据
 */
@ApiModel("")
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class PingIpConfig {

    private Long id;

    private Date addTime;

    private Date updateTime;

    private Integer status;

    private String v6ip1;

    private String v6ip2;

    private String v4ip1;

    private String v4ip2;

    private boolean enabled;
}
