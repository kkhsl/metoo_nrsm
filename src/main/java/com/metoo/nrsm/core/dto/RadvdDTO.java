package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Radvd;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class RadvdDTO extends PageDto<Radvd> {

    @ApiModelProperty("注释")
    private String name;
    @ApiModelProperty("接口ID")
    private Long interfaceId;
    @ApiModelProperty("接口名称")
    private String interfaceName;
    @ApiModelProperty("ipv6前缀")
    private String ipv6Prefix;

}
