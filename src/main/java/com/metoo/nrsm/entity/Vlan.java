package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@ApiModel("Virtual Local Area Network")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Vlan extends IdEntity {

    private String name;
    private Integer number;
    private String description;
    private Date editDate;

    private Long groupId;

    private Long domainId;
    private String domainName;

    @ApiModelProperty("所属子网/网段")
    private String subnet;

    @ApiModelProperty("子网")
    private Long subnetId;
    private String subnetIp;
    private Integer maskBit;

    @ApiModelProperty("子网")
    private Long subnetIdIpv6;
    private String subnetIpv6;
    private Integer maskBitIpv6;


    @ApiModelProperty("显示隐藏 0：显示 1：隐藏")
    private Boolean hidden;
}
