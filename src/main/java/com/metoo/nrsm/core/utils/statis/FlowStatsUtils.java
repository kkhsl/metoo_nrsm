package com.metoo.nrsm.core.utils.statis;

import cn.hutool.core.collection.CollUtil;
import com.metoo.nrsm.core.manager.statis.vo.EchartLineData;
import com.metoo.nrsm.core.manager.statis.vo.EchartLineMonitorData;

import java.util.ArrayList;
import java.util.List;

import static com.metoo.nrsm.core.common.FlowConstants.*;

public final class FlowStatsUtils {

    private static final String START_TIME_STR = "00:00:00";

    private static final String END_TIME_STR = "23:59:59";
//
//    public static FlowStatsConditionVO buildFlowStatsCondition(FlowStatsDimension dimension, int offset) {
//        DateField df = null;
//        if (FlowStatsDimension.statsByHour(dimension)) {
//            df = DateField.HOUR;
//        } else if (FlowStatsDimension.statsByDay(dimension)) {
//            df = DateField.DAY_OF_YEAR;
//        } else if (FlowStatsDimension.statsByMonth(dimension)) {
//            df = DateField.MONTH;
//        } else if (FlowStatsDimension.statsByYear(dimension)) {
//            df = DateField.YEAR;
//        }
//        DateTime curDateTime = DateUtil.offset(new Date(), df, offset);
//        String curDateStr = DateUtil.format(curDateTime, "yyyy-MM-dd");
//        String startTimeSuffix = StrUtil.SPACE + START_TIME_STR;
//        String endTimeSuffix = StrUtil.SPACE + END_TIME_STR;
//        FlowStatsConditionVO condition = null;
//        if (FlowStatsDimension.statsByHour(dimension)) {
//            startTimeSuffix = startTimeSuffix.replace(StrUtil.SPACE + "00", StrUtil.EMPTY);
//            endTimeSuffix = endTimeSuffix.replace(StrUtil.SPACE + "23", StrUtil.EMPTY);
//            int curHour = DateUtil.hour(curDateTime, Boolean.TRUE);
//            String hourStr = curHour < 10 ? "0" + curHour : curHour + "";
//            String startTimeStr = curDateStr + " " + hourStr + startTimeSuffix;
//            String endTimeStr = curDateStr + " " + hourStr + endTimeSuffix;
//            LocalDateTime startTime = DateUtil.parseLocalDateTime(startTimeStr);
//            LocalDateTime endTime = DateUtil.parseLocalDateTime(endTimeStr);
//            condition = new FlowStatsConditionVO(startTime, endTime, curDateStr, hourStr);
//        } else if (FlowStatsDimension.statsByDay(dimension)) {
//            String startTimeStr = curDateStr + startTimeSuffix;
//            String endTimeStr = curDateStr + endTimeSuffix;
//            LocalDateTime startTime = DateUtil.parseLocalDateTime(startTimeStr);
//            LocalDateTime endTime = DateUtil.parseLocalDateTime(endTimeStr);
//            condition = new FlowStatsConditionVO(startTime, endTime, curDateStr);
//        } else if (FlowStatsDimension.statsByMonth(dimension)) {
//            condition = new FlowStatsConditionVO(DateUtil.beginOfMonth(curDateTime).toLocalDateTime(), DateUtil.endOfMonth(curDateTime).toLocalDateTime(), curDateStr);
//        } else if (FlowStatsDimension.statsByYear(dimension)) {
//            condition = new FlowStatsConditionVO(DateUtil.beginOfYear(curDateTime).toLocalDateTime(), DateUtil.endOfYear(curDateTime).toLocalDateTime(), curDateStr);
//        }
//        if (ObjectUtil.isNotNull(condition)) {
//            condition.setFlowStatsDimension(dimension);
//            return condition;
//        }
//        return null;
//    }
//
//    /**
//     * 指定日、月、年过滤条件组装
//     * @param dimension
//     * @param day
//     * @return
//     */
//    public static FlowStatsConditionVO buildFlowStatsConditionByDay(FlowStatsDimension dimension, String day) {
//        DateField df = null;
//        if (FlowStatsDimension.statsByHour(dimension)) {
//            df = DateField.HOUR;
//        } else if (FlowStatsDimension.statsByDay(dimension)) {
//            df = DateField.DAY_OF_YEAR;
//        } else if (FlowStatsDimension.statsByMonth(dimension)) {
//            df = DateField.MONTH;
//        } else if (FlowStatsDimension.statsByYear(dimension)) {
//            df = DateField.YEAR;
//        }
//        DateTime curDateTime = DateUtil.parse(day, DatePattern.NORM_DATETIME_FORMATTER);
//        String curDateStr = DateUtil.format(curDateTime, "yyyy-MM-dd");
//        String startTimeSuffix = StrUtil.SPACE + START_TIME_STR;
//        String endTimeSuffix = StrUtil.SPACE + END_TIME_STR;
//        FlowStatsConditionVO condition = null;
//        if (FlowStatsDimension.statsByHour(dimension)) {
//            startTimeSuffix = startTimeSuffix.replace(StrUtil.SPACE + "00", StrUtil.EMPTY);
//            endTimeSuffix = endTimeSuffix.replace(StrUtil.SPACE + "23", StrUtil.EMPTY);
//            int curHour = DateUtil.hour(curDateTime, Boolean.TRUE);
//            String hourStr = curHour < 10 ? "0" + curHour : curHour + "";
//            String startTimeStr = curDateStr + " " + hourStr + startTimeSuffix;
//            String endTimeStr = curDateStr + " " + hourStr + endTimeSuffix;
//            LocalDateTime startTime = DateUtil.parseLocalDateTime(startTimeStr);
//            LocalDateTime endTime = DateUtil.parseLocalDateTime(endTimeStr);
//            condition = new FlowStatsConditionVO(startTime, endTime, curDateStr, hourStr);
//        } else if (FlowStatsDimension.statsByDay(dimension)) {
//            String startTimeStr = curDateStr + startTimeSuffix;
//            String endTimeStr = curDateStr + endTimeSuffix;
//            LocalDateTime startTime = DateUtil.parseLocalDateTime(startTimeStr);
//            LocalDateTime endTime = DateUtil.parseLocalDateTime(endTimeStr);
//            condition = new FlowStatsConditionVO(startTime, endTime, curDateStr);
//        } else if (FlowStatsDimension.statsByMonth(dimension)) {
//            condition = new FlowStatsConditionVO(DateUtil.beginOfMonth(curDateTime).toLocalDateTime(), DateUtil.endOfMonth(curDateTime).toLocalDateTime(), curDateStr);
//        } else if (FlowStatsDimension.statsByYear(dimension)) {
//            condition = new FlowStatsConditionVO(DateUtil.beginOfYear(curDateTime).toLocalDateTime(), DateUtil.endOfYear(curDateTime).toLocalDateTime(), curDateStr);
//        }
//        if (ObjectUtil.isNotNull(condition)) {
//            condition.setFlowStatsDimension(dimension);
//            return condition;
//        }
//        return null;
//    }

    /**
     * 结果数据变更
     * @param data
     * @return
     */
    public static EchartLineMonitorData buildResult(EchartLineData data){
        EchartLineMonitorData result=EchartLineMonitorData.builder().build();
        if(null!=data) {
            result.setTimeX(data.getTitle());
            if(CollUtil.isNotEmpty(data.getValue())){
                List<Double> ipv4=new ArrayList<>();
                List<Double> ipv6=new ArrayList<>();
                List<Double> ipv6Radio=new ArrayList<>();
                data.getValue().forEach(o->{
                    if(o.getName().equals(IPV4)){
                        ipv4.addAll(o.getData());
                    }
                    if(o.getName().equals(IPV6)){
                        ipv6.addAll(o.getData());
                    }
                    if(o.getName().equals(IPV6RADIO)){
                        ipv6Radio.addAll(o.getData());
                    }
                });
                result.setIpv4(ipv4);
                result.setIpv6(ipv6);
                result.setIpv6Ratio(ipv6Radio);
            }
        }
        return result;
    }


}
