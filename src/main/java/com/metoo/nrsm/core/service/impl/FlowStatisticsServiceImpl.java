package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.NetworkElementDto;
import com.metoo.nrsm.core.mapper.FlowStatisticsMapper;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FlowSummary;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 16:06
 */
@Service
@Transactional
@Slf4j
public class FlowStatisticsServiceImpl implements IFlowStatisticsService {

    @Autowired
    private FlowStatisticsMapper flowStatisticsMapper;

    @Override
    public Page<FlowStatistics> selectObjConditionQuery(NetworkElementDto instance) {
        return null;
    }

    @Override
    public List<FlowSummary> getFlowSummary(Map<String, Object> params) {
        try {
            // 转换参数中的Date
            Map<String, Object> queryParams = new HashMap<>();

            Date startDate = (Date) params.get("startOfDay");
            Date endDate = (Date) params.get("endOfDay");

            // 1. 设置时区（确保与数据库一致）
            TimeZone zone = TimeZone.getTimeZone("Asia/Shanghai");

            // 2. 创建带时区的格式化器
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(zone);

            // 3. 转换为数据库兼容的时间格式
            String startTimeStr = sdf.format(startDate);
            String endTimeStr = sdf.format(endDate);

            queryParams.put("startTime", startTimeStr);
            queryParams.put("endTime", endTimeStr);
            // 4. 添加额外的条件
            return flowStatisticsMapper.getFlowSummary(queryParams);
        } catch (Exception e) {
            log.error("获取流量摘要失败", e);
            throw new ServiceException("获取流量摘要失败");
        }
    }

    @Override
    public List<FlowStatistics> selectObjByMap(Map params) {
        return this.flowStatisticsMapper.selectObjByMap(params);
    }

    @Override
    public List<FlowStatistics> selectObjByMap1(Map params) {
        return this.flowStatisticsMapper.selectObjByMap1(params);
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

    @Override
    public boolean save1(FlowStatistics instance) {
        try {
            this.flowStatisticsMapper.save1(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
