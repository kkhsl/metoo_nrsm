package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.UnitFlowStats;

public interface IUnitFlowStatsService {

    // 查询某一维度的统计记录
    UnitFlowStats selectStatsByDimension(Long unit_id, String stats_dimension, Integer day, Integer month, Integer year);

    boolean update(UnitFlowStats unitFlowStats);

    boolean save(UnitFlowStats instance);
}
