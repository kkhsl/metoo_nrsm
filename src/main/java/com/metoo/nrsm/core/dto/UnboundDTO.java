package com.metoo.nrsm.core.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class UnboundDTO {

    private Boolean privateAddress;

    @ApiModelProperty("转发地址：上游 DNS 服务器地址")
    private List<String> forwardAddress;

    @ApiModelProperty("本地 DNS 区域")
    private List<LocalZoneDTO> localZone = new ArrayList<>();

    @ApiModelProperty("用于指定某个域名的静态解析记录")
    private List<LocalDataDTO> localData = new ArrayList<>();

}
