package com.metoo.nrsm.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 采集中间json对象DTO
 * @author zzy
 * @version 1.0
 * @date 2024-10-06 14:53
 */
@ApiModel("采集中间json对象DTO")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PortIpv4AndIpv6Dto {

    @ApiModelProperty("Id")
    private Integer id;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("ipv4地址")
    private String ip;

    @ApiModelProperty("端口")
    private String port;

    @ApiModelProperty("1：up，2：down")
    private Integer status;

    @ApiModelProperty("掩码位")
    private String mask;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;


    @ApiModelProperty("ip地址")
    private String ipv6_address;

    @ApiModelProperty("掩码位")
    private String ipv6_subnet;

}
