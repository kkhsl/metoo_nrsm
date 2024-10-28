package com.metoo.nrsm.entity;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-19 11:55
 */
public class IpCombination {

    private String ip;
    private String mac;
    private String port;

    @ApiModelProperty("设备名称")
    private String deviceIp;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("设备类型")
    private String deviceType;
    @ApiModelProperty("标记")
    private String tag;

    private String v4ip1;
    private String v4ip2;
    private String v4ip3;

    private String v6ip1;
    private String v6ip2;
    private String v6ip3;
}
