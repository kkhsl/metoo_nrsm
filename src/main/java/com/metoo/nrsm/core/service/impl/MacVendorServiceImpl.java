package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.MacVendorMapper;
import com.metoo.nrsm.core.service.IMacVendorService;
import com.metoo.nrsm.entity.MacVendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MacVendorServiceImpl implements IMacVendorService {

    @Autowired
    private MacVendorMapper macVendorMapper;

    @Override
    public List<MacVendor> selectObjByMap(Map params) {
        return this.macVendorMapper.selectObjByMap(params);
    }
}
