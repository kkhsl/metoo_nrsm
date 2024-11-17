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
    private String zoneName;
    @ApiModelProperty("区域类型，如 static、redirect")
    private String zoneType; // 行为类型

}
