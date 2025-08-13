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
    public UnitFlowStats selectStatsByDimension(Long unitId, String statsDimension, Integer day, Integer month, Integer year) {
        // 查询具体维度的统计记录
        return unitFlowStatsMapper.selectStatsByDimension(unitId, statsDimension, day, month, year);
    }

    @Override
    public boolean update(UnitFlowStats instance) {
        try {
            return unitFlowStatsMapper.update(instance) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean save(UnitFlowStats instance) {
        try {
            return unitFlowStatsMapper.insert(instance) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
