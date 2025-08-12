package com.metoo.nrsm.core.traffic.utils;

import com.metoo.nrsm.core.service.IUnitHourFlowStatsService;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.UnitHourFlowStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class UnitFlowUtils {

    private final IUnitHourFlowStatsService unitHourFlowStatsService;

    public UnitFlowUtils(IUnitHourFlowStatsService unitHourFlowStatsService){
        this.unitHourFlowStatsService = unitHourFlowStatsService;
    }

    public void saveFlow(List<FlowUnit> flowUnits, LocalDateTime localDateTime){
        List<UnitHourFlowStats> unitHourFlowStatsList = new ArrayList<>();
        if(flowUnits != null && flowUnits.size() > 0){
            for (FlowUnit flowUnit : flowUnits) {
                Long unitId = flowUnit.getUnitId();
                String vfourFlow = flowUnit.getVfourFlow();
                String vsixFlow = flowUnit.getVsixFlow();
                UnitHourFlowStats unitHourFlowStats = new UnitHourFlowStats();
                unitHourFlowStats.setUnit_id(unitId);
                try {
                    unitHourFlowStats.setIpv4(Double.parseDouble(vfourFlow));
                    unitHourFlowStats.setIpv6(Double.parseDouble(vsixFlow));
                } catch (NumberFormatException e) {
                    // 处理转换失败的情况（如设为null或默认值）
                    unitHourFlowStats.setIpv4(null);
                    unitHourFlowStats.setIpv6(null);
                    log.error("流量数据格式错误: vfourFlow={}, vsixFlow={}", vfourFlow, vsixFlow, e);
                }
                fillTimeFields(localDateTime, unitHourFlowStats);
                unitHourFlowStatsList.add(unitHourFlowStats);
            }
            unitHourFlowStatsService.batchSave(unitHourFlowStatsList);
        }
    }

    // 定义时间格式常量
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static void fillTimeFields(LocalDateTime baseTime, UnitHourFlowStats stats) {
        // 设置统计时间（直接使用LocalDateTime）
        stats.setStats_time(Date.from(baseTime.atZone(ZoneId.systemDefault()).toInstant()));

        // 生成hour_time（yyyyMMddHH格式）
        String hourTimeStr = baseTime.format(HOUR_FORMATTER);
        stats.setHour_time(Integer.parseInt(hourTimeStr));

        // 生成day（yyyyMMdd格式）
        String dayStr = baseTime.format(DAY_FORMATTER);
        stats.setDay(Integer.parseInt(dayStr));
    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        System.out.println(date);
        System.out.println(date.getClass());
    }
}
