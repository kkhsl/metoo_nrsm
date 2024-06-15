package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("ip地址表")
public class Ipv4Detail extends IdEntity {

    private String deviceName;
    @ApiModelProperty("Ip地址")
    private String ip;
    @ApiModelProperty("Ip地址")
    private String ipSegment;
    @ApiModelProperty("Mac地址")
    private String mac;
    @ApiModelProperty("接口序号")
    private Integer sequence;
    @ApiModelProperty("在线/离线")
    private boolean online;
    @ApiModelProperty("")
    private int time;
    @ApiModelProperty("")
    private String duration;
    @ApiModelProperty("")
    private Integer usage;
    @ApiModelProperty("")
    private String description;


}
