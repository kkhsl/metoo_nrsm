package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FlowSummary;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 16:07
 */
@Mapper
public interface FlowStatisticsMapper {
    List<FlowSummary> getFlowSummary(Map<String, Object> params);

    List<FlowStatistics> selectObjByMap(Map params);

    int save(FlowStatistics instance);
    int save1(FlowStatistics instance);
}
