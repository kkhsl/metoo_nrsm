package com.metoo.nrsm.core.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class LocalDataDTO {

    @ApiModelProperty("主机名：需要解析的域名")
    private String hostName;
    @ApiModelProperty("记录类型 可选 A, AAAA, CNAME 等")
    private String recordType;
    @ApiModelProperty("映射地址：域名对应的 IP 地址")
    private String mappedAddress;

}
