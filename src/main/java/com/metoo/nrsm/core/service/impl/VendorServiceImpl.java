package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.VendorMapper;
import com.metoo.nrsm.core.service.IVendorService;
import com.metoo.nrsm.entity.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class VendorServiceImpl implements IVendorService {

    @Autowired
    private VendorMapper vendorMapper;

    @Override
    public Vendor selectObjById(Long id) {
        return this.vendorMapper.selectObjById(id);
    }

    @Override
    public Vendor selectObjByName(String name) {
        return this.vendorMapper.selectObjByName(name);
    }

    @Override
    public List<Vendor> selectObjByMap(Map params) {
        return this.vendorMapper.selectObjByMap(params);
    }


    @Override
    public List<Vendor> selectConditionQuery(Map params) {
        return this.vendorMapper.selectConditionQuery(params);
    }
}
