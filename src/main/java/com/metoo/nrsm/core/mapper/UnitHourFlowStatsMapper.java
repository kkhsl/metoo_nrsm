package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.UnitHourFlowStats;

import java.util.List;

public interface UnitHourFlowStatsMapper {

    int save(UnitHourFlowStats instance);

    int batchInsert(List<UnitHourFlowStats> unitHourFlowStatsList);
}
