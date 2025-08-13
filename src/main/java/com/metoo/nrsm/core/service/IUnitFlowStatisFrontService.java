package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.manager.statis.vo.EchartLineData;
import com.metoo.nrsm.core.manager.statis.vo.EchartLineMonitorData;
import com.metoo.nrsm.core.manager.statis.vo.FlowRadioData;
import com.metoo.nrsm.entity.UnitFlowStats;

import java.util.List;

public interface IUnitFlowStatisFrontService {
    /**
     * 所有部门流量情况排名列表-日、月、年
     * @param statsDimension
     * @param filter
     * @return
     */
    List<FlowRadioData> allOrgDayStats(String statsDimension, String filter);

    /**
     * 查询所有部门流量情况分-日、月、年
     * @param statsDimension
     * @param filter
     * @return
     */
    EchartLineMonitorData statsByDimension(String statsDimension, String filter);
    /**
     * 根据部门编码获取单位日、月度、年流量统计
     */
    EchartLineData orgFlowStatsById(Long id, String statsDimension, String filter);

    boolean save(UnitFlowStats stats);



    List<FlowRadioData> queryStatsByTime(String startTime,String endTime);
}
