package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.manager.statis.vo.FlowRadioData;
import com.metoo.nrsm.entity.UnitHourFlowStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UnitHourFlowStatsMapper {

    int save(UnitHourFlowStats instance);

    List<FlowRadioData> orgHour(@Param("id") Long id,@Param("day") Long day);
}
