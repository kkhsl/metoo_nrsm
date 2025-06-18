package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.UnitSubnetMapper;
import com.metoo.nrsm.core.service.IUnitSubnetService;
import com.metoo.nrsm.entity.UnitSubnet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class UnitSubnetServiceImpl implements IUnitSubnetService {

    @Resource
    private UnitSubnetMapper unitSubnetMapper;

    @Override
    public List<UnitSubnet> selectObjAll() {
        return this.unitSubnetMapper.selectObjAll();
    }
}
