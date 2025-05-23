package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Radvd extends IdEntity {

    @ApiModelProperty("注释")
    private String name;
    @ApiModelProperty("接口ID")
    private Long interfaceId;
    @ApiModelProperty("接口名称")
    private String interfaceName;
    @ApiModelProperty("ipv6前缀")
    private String ipv6Prefix;

}
