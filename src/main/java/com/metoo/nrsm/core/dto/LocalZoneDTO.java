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
public class LocalZoneDTO {

    private String zoneName;
    @ApiModelProperty("区域类型，如 static、redirect")
    private String zoneType; // 行为类型

    private List<LocalDataDTO> localData = new ArrayList<>();
}
