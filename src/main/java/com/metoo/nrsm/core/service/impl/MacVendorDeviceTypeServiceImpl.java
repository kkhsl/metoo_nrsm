package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.MacVendorDeviceTypeMapper;
import com.metoo.nrsm.core.service.IMacVendorDeviceTypeService;
import com.metoo.nrsm.entity.MacVendorDeviceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class MacVendorDeviceTypeServiceImpl implements IMacVendorDeviceTypeService {

    @Resource
    private MacVendorDeviceTypeMapper macVendorDeviceTypeMapper;

    @Override
    public MacVendorDeviceType selectObjByMacVendor(String macVendor) {
        return this.macVendorDeviceTypeMapper.selectObjByMacVendor(macVendor);
    }
}
