package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.UnitHourFlowStats;

import java.util.List;

public interface IUnitHourFlowStatsService {

    boolean save(UnitHourFlowStats instance);

    boolean batchSave(List<UnitHourFlowStats> unitHourFlowStatsList);
}
