package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.manager.statis.vo.FlowRadioData;
import com.metoo.nrsm.entity.UnitFlowStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UnitFlowStatsMapper {
    /**
     * 所有部门流量情况排名列表-日、月、年
     * @param statsDimension
     * @param filter
     * @return
     */
    List<FlowRadioData> allOrgDayStats(@Param("statsDimension") String statsDimension,@Param("filter") Long filter);

    List<FlowRadioData> busiDay(Integer month);

    List<FlowRadioData> busiMonth(Integer year);

    List<UnitFlowStats> queryList(@Param("id") Long id, @Param("month") Integer month,@Param("year") Integer year,@Param("statsDimension") String statsDimension);

    // 查询某一维度的统计记录
    UnitFlowStats selectStatsByDimension(
            @Param("unit_id") Long unitId,
            @Param("stats_dimension") String statsDimension,
            @Param("day") Integer day,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
    int update(UnitFlowStats stats);

    int insert(UnitFlowStats stats);

}
