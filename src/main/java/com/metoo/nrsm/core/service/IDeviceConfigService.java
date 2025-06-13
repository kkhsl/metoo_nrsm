package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.DeviceConfigDTO;
import com.metoo.nrsm.entity.DeviceConfig;

public interface IDeviceConfigService {
    Page<DeviceConfig> selectAll(DeviceConfigDTO instance);
}

