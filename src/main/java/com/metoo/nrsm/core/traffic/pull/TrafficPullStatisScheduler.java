package com.metoo.nrsm.core.traffic.pull;

import com.metoo.nrsm.core.config.ssh.utils.DateUtils;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.IUnitFlowStatsService;
import com.metoo.nrsm.core.service.IUnitHourFlowStatsService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.traffic.push.utils.TrafficPushApiUtils;
import com.metoo.nrsm.core.traffic.utils.TrafficUtils;
import com.metoo.nrsm.core.traffic.utils.UnitFlowUtils;
import com.metoo.nrsm.core.utils.string.StringUtils;
import com.metoo.nrsm.core.vo.UnitVO;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Unit;
import com.metoo.nrsm.entity.UnitFlowStats;
import com.metoo.nrsm.entity.UnitHourFlowStats;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Configuration
public class TrafficPullStatisScheduler {

    @Autowired
    private IFlowUnitService flowUnitService;
    @Autowired
    private IUnitHourFlowStatsService unitHourFlowStatsService;
    @Autowired
    private IUnitFlowStatsService unitFlowStatsService;

    @Value("${task.switch.traffic.api.is-open}")
    private boolean trafficApi;

    private final ReentrantLock trafficApiLock = new ReentrantLock();

    // 每天在 23:59:59 执行任务
    @Scheduled(cron = "59 59 23 * * ?")
    public void flowStateByMonth() {
        if (!trafficApi) {
            return;
        }

        if (!trafficApiLock.tryLock()) {
            log.info("月流量统计任务正在执行中，跳过本次调度");
            return;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("hidden", false);
            List<FlowUnit> flowUnits = flowUnitService.selectObjByMap(params);

            if (CollectionUtils.isEmpty(flowUnits)) {
                log.info("未获取到任何单位，跳过月流量统计");
                return;
            }

            for (FlowUnit flowUnit : flowUnits) {
                String unitName = flowUnit.getUnitName();
                Long unitId = flowUnit.getUnitId();

                if (StringUtils.isBlank(unitName)) {
                    log.warn("单位ID[{}]名称为空，跳过月统计", unitId);
                    continue;
                }

                // 获取当前日期（yyyyMMdd格式）
                // 统计当天流量
                LocalDateTime baseTime = TimeUtils.getNow();
                int currentDay = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
                int currentMonth = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")));
                int currentYear = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")));

                // 获取当日总流量
                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("unitId", unitId);
                queryParams.put("day", currentDay);

                Map<String, Double> dailyTotal = unitHourFlowStatsService.selectDailyTotalFlow(queryParams);

                // 处理统计结果
                if (dailyTotal == null || dailyTotal.isEmpty()) {
                    log.info("单位[{}](ID:{})当日({})无流量数据", unitName, unitId, currentDay);
                } else {
                    Double totalIPv4 = dailyTotal.get("totalIPv4");
                    Double totalIPv6 = dailyTotal.get("totalIPv6");

                    // 1. 日记录处理
                    handleFlowStats(unitId, unitName, totalIPv4, totalIPv6, "1", currentDay, currentMonth, currentYear, baseTime);

                    // 2. 月记录处理
                    handleFlowStats(unitId, unitName, totalIPv4, totalIPv6, "2", currentDay, currentMonth, currentYear, baseTime);

                    // 3. 年记录处理
                    handleFlowStats(unitId, unitName, totalIPv4, totalIPv6, "3", currentDay, currentMonth, currentYear, baseTime);

                    log.info("单位[{}](ID:{})当日({})统计结果 - IPv4: {}, IPv6: {}",
                            unitName, unitId, currentDay, totalIPv4, totalIPv6);
                }
            }
        } catch (Exception e) {
            log.error("月流量统计任务执行异常", e);
        } finally {
            trafficApiLock.unlock();
        }
    }

    private void handleFlowStats(Long unitId, String unitName, Double ipv4, Double ipv6, String statsDimension, Integer day, Integer month, Integer year, LocalDateTime baseTime) {
        UnitFlowStats existingStats = unitFlowStatsService.selectStatsByDimension(unitId, statsDimension, day, month, year);
        if (existingStats == null) {
            UnitFlowStats newStats = new UnitFlowStats();
            newStats.setUnit_id(unitId);
            newStats.setUnit_name(unitName);
            newStats.setIpv4(ipv4);
            newStats.setIpv6(ipv6);
            newStats.setStats_dimension(statsDimension);
            newStats.setStats_time(Date.from(baseTime.atZone(ZoneId.systemDefault()).toInstant()));
            if ("1".equals(statsDimension)) {  // 日统计
                newStats.setDay(day);
                newStats.setMonth(month);
                newStats.setYear(year);
            } else if ("2".equals(statsDimension)) {  // 月统计
                newStats.setMonth(month);
                newStats.setYear(year);
            } else if ("3".equals(statsDimension)) {  // 年统计
                newStats.setYear(year);
            }
            unitFlowStatsService.save(newStats);
        } else {
            existingStats.setIpv4(existingStats.getIpv4() + ipv4);
            existingStats.setIpv6(existingStats.getIpv6() + ipv6);
            existingStats.setStats_time(Date.from(baseTime.atZone(ZoneId.systemDefault()).toInstant()));
            unitFlowStatsService.update(existingStats);
        }
    }


//    // 每日凌晨1点执行
////    @Scheduled(cron = "0 0 1 * * ?")
//    public void flowStateByDay(){
//        if (trafficApi) {
//            if (trafficApiLock.tryLock()) {
//                try {
//                    Map params = new HashMap();
//                    params.put("hidden", false);
//                    List<FlowUnit> flowUnits = flowUnitService.selectObjByMap(params);
//                    if (flowUnits == null || flowUnits.isEmpty()) {
//                        log.info("未获取到任何单位，跳过调用 NetFlow API。");
//                        return;
//                    }
//                    for (FlowUnit flowUnit : flowUnits) {
//
//                        String unitName = flowUnit.getUnitName();
//                        Long unitId = flowUnit.getUnitId();
//                        if (unitName == null || unitName.trim().isEmpty()) {
//                            log.warn("单位名称为空，跳过该单位的流量查询。");
//                            continue;
//                        }
//                        // 统计
//                        // 获取当前日期（yyyyMMdd格式）
//                        LocalDateTime baseTime = TimeUtils.getNow();
//                        int currentDay = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
//
//                        // 获取当日总流量
//                        Map<String, Object> queryParams = new HashMap<>();
//                        queryParams.put("unitId", unitId);
//                        queryParams.put("day", currentDay);
//
//                        Map<String, Double> dailyTotal = unitHourFlowStatsService.selectDailyTotalFlow(queryParams);
//
//                        // 处理统计结果
//                        if (dailyTotal == null || dailyTotal.isEmpty()) {
//                            log.info("单位[{}](ID:{})当日({})无流量数据", unitName, unitId, currentDay);
//                        } else {
//                            Double totalIPv4 = dailyTotal.get("totalIPv4");
//                            Double totalIPv6 = dailyTotal.get("totalIPv6");
//
//                            log.info("单位[{}](ID:{})当日({})统计结果 - IPv4: {}, IPv6: {}",
//                                    unitName, unitId, currentDay, totalIPv4, totalIPv6);
//
//                            // 6. 保存或处理统计结果（示例）
//                            unitFlowUtils.saveUnitFlowStats(unitId, currentDay, totalIPv4, totalIPv6, baseTime);
//                        }
//
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }finally {
//                    if(trafficApiLock != null){
//                        trafficApiLock.unlock();
//                    }
//                }
//            }
//        }
//    }
}
