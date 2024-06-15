package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.DeviceType;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("设备类型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTypeDTO extends PageDto<DeviceType> {

    private String name;
    private Integer type;

}
