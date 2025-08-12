package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.UnitFlowStatsMapper;
import com.metoo.nrsm.core.service.IUnitFlowStatsService;
import com.metoo.nrsm.entity.UnitFlowStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnitFlowStatsServiceImpl implements IUnitFlowStatsService {

    private final UnitFlowStatsMapper unitFlowStatsMapper;
    @Autowired
    public UnitFlowStatsServiceImpl(UnitFlowStatsMapper unitFlowStatsMapper){
        this.unitFlowStatsMapper = unitFlowStatsMapper;
    }

    @Override
    public int save(UnitFlowStats instance) {
        return unitFlowStatsMapper.save(instance);
    }

}
