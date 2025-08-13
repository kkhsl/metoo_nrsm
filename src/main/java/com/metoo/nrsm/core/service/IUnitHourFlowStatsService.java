package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.UnitHourFlowStats;

import java.util.List;
import java.util.Map;

public interface IUnitHourFlowStatsService {

    List<UnitHourFlowStats> selectObjByMap(Map<String, Object> params);

    Map<String, Double> selectMonthlyTotalFlow(Map<String, Object> params);

    Map<String, Double> selectDailyTotalFlow(Map<String, Object> params);

    UnitHourFlowStats selectByUnitIdAndTime(Long unitId, Integer year, Integer month, Integer day, Integer hour);

    boolean update(UnitHourFlowStats unitHourFlowStats);

    boolean save(UnitHourFlowStats instance);

    boolean batchSave(List<UnitHourFlowStats> unitHourFlowStatsList);
}
