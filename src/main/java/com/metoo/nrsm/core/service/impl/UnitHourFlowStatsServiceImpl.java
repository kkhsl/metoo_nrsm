package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.UnitHourFlowStatsMapper;
import com.metoo.nrsm.core.service.IUnitHourFlowStatsService;
import com.metoo.nrsm.entity.UnitHourFlowStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnitHourFlowStatsServiceImpl implements IUnitHourFlowStatsService {

    private final UnitHourFlowStatsMapper unitHourFlowStatsMapper;
    public UnitHourFlowStatsServiceImpl(UnitHourFlowStatsMapper unitHourFlowStatsMapper){
        this.unitHourFlowStatsMapper = unitHourFlowStatsMapper;
    }

    @Override
    public boolean save(UnitHourFlowStats instance) {
        try {
            return unitHourFlowStatsMapper.save(instance) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
