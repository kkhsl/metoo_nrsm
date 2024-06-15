package com.metoo.nrsm.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 16:24
 */
@ApiModel("")
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class PingIpConfig {

    private Long id;

    private Integer status;

    private String v6ip1;

    private String v6ip2;
}
