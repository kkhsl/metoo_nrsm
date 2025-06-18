package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MacVendorDeviceType extends IdEntity {

    @ApiModelProperty("mac品牌")
    private String macVendor;
    @ApiModelProperty("设备类型Id")
    private Long deviceTypeId;
}
