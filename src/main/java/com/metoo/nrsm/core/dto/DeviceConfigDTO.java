package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.DeviceConfig;
import lombok.Data;

import java.util.Date;

@Data
public class DeviceConfigDTO extends PageDto<DeviceConfig> {
    private String name;
    private Date time;
    private Integer type;
    private String content;
}