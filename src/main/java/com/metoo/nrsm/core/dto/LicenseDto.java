package com.metoo.nrsm.core.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class LicenseDto {

    @ApiModelProperty("申请码,系统唯一序列号")
    private String systemSN;

    @ApiModelProperty("开始时间")
    private long startTime;

    @ApiModelProperty("结束时间")
    private long endTime;

    @ApiModelProperty("License类型 0：试用版 1，授权版 2：终身版")
    private String type;

    @ApiModelProperty("License版本号")
    private String licenseVersion;

    @ApiModelProperty("过期时间")
    private long expireTime;

    // 授权信息
    @ApiModelProperty("授权防火墙")
    private int licenseFireWall;
    @ApiModelProperty("授权路由/交换")
    private int licenseRouter;
    @ApiModelProperty("授权主机数")
    private int licenseHost;
    @ApiModelProperty("授权模拟网关")
    private int licenseUe;
}
