package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.DeviceConfigDTO;
import com.metoo.nrsm.core.mapper.DeviceConfigMapper;
import com.metoo.nrsm.core.service.IDeviceConfigService;
import com.metoo.nrsm.entity.DeviceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceConfigServiceImpl implements IDeviceConfigService {
    @Autowired
    private DeviceConfigMapper deviceConfigMapper;
    @Override
    public Page<DeviceConfig> selectAll(DeviceConfigDTO instance) {
        if(instance == null){
            instance = new DeviceConfigDTO();
        }
        Page<DeviceConfig> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        deviceConfigMapper.selectAll(instance);
        return page;
    }
}
