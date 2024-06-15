package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@ApiModel("IP地址表")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Address extends IdEntity {


    @ApiModelProperty("Ip地址")
    private String ip;
    @ApiModelProperty("Mac地址")
    private String mac;
    @ApiModelProperty("主机名称")
    private String hostName;
    @ApiModelProperty("子网Id")
    private Long subnetId;
    @ApiModelProperty("描述")
    private String description;

    private Ipv4Detail ipDetail;
    private Map deviceInfo = new HashMap();

    private String location;
    private Long departmentId;
    private String duty;

}
