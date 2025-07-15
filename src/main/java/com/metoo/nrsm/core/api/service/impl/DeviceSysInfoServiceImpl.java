package com.metoo.nrsm.core.api.service.impl;

import com.metoo.nrsm.core.api.dto.DeviceSysInfoDTO;
import com.metoo.nrsm.core.api.mapper.DeviceSysInfoMapper;
import com.metoo.nrsm.core.api.service.IDeviceSysInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DeviceSysInfoServiceImpl implements IDeviceSysInfoService {

    private DeviceSysInfoMapper deviceSysInfoMapper;
    @Autowired
    public void setDeviceSysInfoMapper(DeviceSysInfoMapper deviceSysInfoMapper) {
        this.deviceSysInfoMapper = deviceSysInfoMapper;
    }

    @Autowired  // 在Spring 4.3+可以省略，如果有单个构造器
    public DeviceSysInfoServiceImpl(DeviceSysInfoMapper deviceSysInfoMapper) {
        this.deviceSysInfoMapper = deviceSysInfoMapper;
    }

    @Override
    public int insert(DeviceSysInfoDTO list) {
        list.setAddTime(new Date());
        list.setUpdateTime(new Date());
        return deviceSysInfoMapper.insert(list);
    }
}
