package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.NetworkElementDto;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FlowSummary;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 16:06
 */
public interface IFlowStatisticsService {

    Page<FlowStatistics> selectObjConditionQuery(NetworkElementDto instance);


    List<FlowSummary> getFlowSummary(Map<String, Object> params);

    List<FlowStatistics> selectObjByMap(Map params);

    boolean save(FlowStatistics instance);
}
