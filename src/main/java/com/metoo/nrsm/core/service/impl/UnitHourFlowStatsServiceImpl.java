package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.UnitHourFlowStatsMapper;
import com.metoo.nrsm.core.service.IUnitHourFlowStatsService;
import com.metoo.nrsm.entity.UnitHourFlowStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UnitHourFlowStatsServiceImpl implements IUnitHourFlowStatsService {

    private final UnitHourFlowStatsMapper unitHourFlowStatsMapper;
    public UnitHourFlowStatsServiceImpl(UnitHourFlowStatsMapper unitHourFlowStatsMapper){
        this.unitHourFlowStatsMapper = unitHourFlowStatsMapper;
    }

    @Override
    public List<UnitHourFlowStats> selectObjByMap(Map<String, Object> params) {
        return unitHourFlowStatsMapper.selectObjByMap(params);
    }

    @Override
    public Map<String, Double> selectDailyTotalFlow(Map<String, Object> params) {
        return unitHourFlowStatsMapper.selectDailyTotalFlow(params);
    }

    @Override
    public UnitHourFlowStats selectByUnitIdAndTime(Long unitId, Integer year, Integer month, Integer day, Integer hour) {
        return unitHourFlowStatsMapper.selectByUnitIdAndTime(unitId, year, month, day, hour);
    }

    @Override
    public boolean update(UnitHourFlowStats instance) {
        try {
            return unitHourFlowStatsMapper.update(instance) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    @Override
    public boolean batchSave(List<UnitHourFlowStats> unitHourFlowStatsList) {
        try {
            return unitHourFlowStatsMapper.batchInsert(unitHourFlowStatsList) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Double> selectMonthlyTotalFlow(Map<String, Object> params) {
        return unitHourFlowStatsMapper.selectMonthlyTotalFlow(params);
    }


}
