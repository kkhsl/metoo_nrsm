package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.manager.statis.vo.FlowRadioData;
import com.metoo.nrsm.entity.UnitFlowStats;
import com.metoo.nrsm.entity.UnitHourFlowStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import java.util.List;
import java.util.Map;

public interface UnitHourFlowStatsMapper {

    List<UnitHourFlowStats> selectObjByMap(Map<String, Object> params);

    Map<String, Double> selectMonthlyTotalFlow(Map<String, Object> params);

    Map<String, Double> selectDailyTotalFlow(Map<String, Object> params);

    UnitHourFlowStats selectByUnitIdAndTime(@Param("unit_id") Long unitId,
                                @Param("year") Integer year,
                                @Param("month") Integer month,
                                @Param("day") Integer day,
                                @Param("hour_time") Integer hour_time);

    int update(UnitHourFlowStats unitHourFlowStats);

    int save(UnitHourFlowStats instance);

    int batchInsert(List<UnitHourFlowStats> unitHourFlowStatsList);

    List<FlowRadioData> orgHour(@Param("id") Long id,@Param("day") Long day);
}
