package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.NetworkElementDto;
import com.metoo.nrsm.core.mapper.FlowStatisticsMapper;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.entity.FlowStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 16:06
 */
@Service
@Transactional
public class FlowStatisticsServiceImpl implements IFlowStatisticsService {

    @Autowired
    private FlowStatisticsMapper flowStatisticsMapper;

    @Override
    public Page<FlowStatistics> selectObjConditionQuery(NetworkElementDto instance) {
        return null;
    }

    @Override
    public List<FlowStatistics> selectObjByMap(Map params) {
        return this.flowStatisticsMapper.selectObjByMap(params);
    }

    @Override
    public boolean save(FlowStatistics instance) {
        try {
            this.flowStatisticsMapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
