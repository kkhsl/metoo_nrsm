package com.metoo.nrsm.core.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.core.service.IFluxConfigService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FluxConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class FluxScheduleUtils {

    @Autowired
    private IFluxConfigService fluxConfigService;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;

    private static final ExecutorService SNMP_EXECUTOR = Executors.newFixedThreadPool(20);


    @Scheduled(cron = "0 */5 * * * ?")
    public Result gather() {
        log.info("设备流量统计开始");
        long start = System.currentTimeMillis();
        final Date date = new Date();
        final int DECIMAL_SCALE = 10;
        final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

        final Date historyDate = DateUtils.addSeconds(date, -305);

        List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(null);
        if (fluxConfigs.isEmpty()) {
            return ResponseUtil.ok();
        }

        List<CompletableFuture<Map<String, String>>> v4Futures = new ArrayList<>();
        List<CompletableFuture<Map<String, String>>> v6Futures = new ArrayList<>();

        for (FluxConfig config : fluxConfigs) {
            // IPv4
            List<List<String>> v4Oids = JSONObject.parseObject(config.getIpv4Oid(), List.class);
            if (CollectionUtils.isNotEmpty(v4Oids)) {
                for (List<String> oid : v4Oids) {
                    if (oid.size() >= 2) {
                        v4Futures.add(CompletableFuture.supplyAsync(() -> execSNMP(config, oid.get(0), oid.get(1), false),
                                SNMP_EXECUTOR
                        ));
                    }
                }
            }

            // IPv6
            List<List<String>> v6Oids = JSONObject.parseObject(config.getIpv6Oid(), List.class);
            if (CollectionUtils.isNotEmpty(v6Oids)) {
                for (List<String> oid : v6Oids) {
                    if (oid.size() >= 2) {
                        v6Futures.add(CompletableFuture.supplyAsync(() -> execSNMP(config, oid.get(0), oid.get(1), true)
                        ));
                    }
                }
            }
        }

        // 结果
        BigDecimal ipv4Sum = CompletableFuture.allOf(v4Futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> v4Futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .map(map -> new BigDecimal(map.get("in")).add(new BigDecimal(map.get("out"))))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                ).join();

        BigDecimal ipv6Sum = CompletableFuture.allOf(v6Futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> v6Futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .map(map -> new BigDecimal(map.get("in")).add(new BigDecimal(map.get("out"))))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                ).join();

        // 查询
        Map<String, Object> params = new HashMap<>();
        params.put("startOfDay", historyDate);
        params.put("endOfDay", date);
        params.put("orderBy","addTime");
        params.put("orderType","desc");
        List<FlowStatistics> historyStats = flowStatisticsService.selectObjByMap(params);    //flux

        FlowStatistics currentStat = new FlowStatistics();
        currentStat.setAddTime(date);
        currentStat.setIpv4Sum(ipv4Sum);
        currentStat.setIpv6Sum(ipv6Sum);
        flowStatisticsService.save1(currentStat);

        // 6. 数据处理计算
        if (!historyStats.isEmpty()) {
            FlowStatistics historyStat = historyStats.get(0);
            BigDecimal ipv4Delta = ipv4Sum.subtract(historyStat.getIpv4Sum());
            BigDecimal ipv6Delta = ipv6Sum.subtract(historyStat.getIpv6Sum());

            // 7. 封装数据处理逻辑
            if (ipv4Delta.compareTo(BigDecimal.ZERO) >= 0 &&
                    ipv6Delta.compareTo(BigDecimal.ZERO) >= 0) {

                BigDecimal totalDelta = ipv4Delta.add(ipv6Delta);
                if (totalDelta.compareTo(BigDecimal.ZERO) != 0) {
                    currentStat.setIpv6Rate(ipv6Delta.divide(totalDelta, DECIMAL_SCALE, ROUNDING_MODE));
                }
                // 单位转换(MB)
                currentStat.setIpv4Sum(ipv4Delta.divide(BigDecimal.valueOf(1000000), DECIMAL_SCALE, ROUNDING_MODE));
                currentStat.setIpv6Sum(ipv6Delta.divide(BigDecimal.valueOf(1000000), DECIMAL_SCALE, ROUNDING_MODE));
            }else {
                //处理负值
                Map map=new HashMap();
                map.put("orderBy","addTime");
                map.put("orderType","desc");
                currentStat = flowStatisticsService.selectObjByMap1(map).get(0);
                currentStat.setAddTime(date);
            }
            flowStatisticsService.save(currentStat);
        }

        log.info("设备流量统计，耗时：{}ms", System.currentTimeMillis() - start);
        return ResponseUtil.ok();
    }

//    @Scheduled(cron = "0 */5 * * * ?")
//    public Result gather() {
//        long start = System.currentTimeMillis();
//        final Date date = new Date();
//        final int DECIMAL_SCALE = 10;
//        final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
//
//        final Date historyDate = DateUtils.addSeconds(date, -305);
//
//        List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(null);
//        if (fluxConfigs.isEmpty()) {
//            return ResponseUtil.ok();
//        }
//
//        List<CompletableFuture<Map<String, String>>> v4Futures = new ArrayList<>();
//        List<CompletableFuture<Map<String, String>>> v6Futures = new ArrayList<>();
//
//        for (FluxConfig config : fluxConfigs) {
//            // IPv4
//            List<List<String>> v4Oids = JSONObject.parseObject(config.getIpv4Oid(), List.class);
//            if (CollectionUtils.isNotEmpty(v4Oids)) {
//                for (List<String> oid : v4Oids) {
//                    if (oid.size() >= 2) {
//                        v4Futures.add(CompletableFuture.supplyAsync(() ->
//                                        execSNMP(config, oid.get(0), oid.get(1), false),
//                                SNMP_EXECUTOR
//                        ));
//                    }
//                }
//            }
//
//            // IPv6
//            List<List<String>> v6Oids = JSONObject.parseObject(config.getIpv6Oid(), List.class);
//            if (CollectionUtils.isNotEmpty(v6Oids)) {
//                for (List<String> oid : v6Oids) {
//                    if (oid.size() >= 2) {
//                        v6Futures.add(CompletableFuture.supplyAsync(() ->
//                                        execSNMP(config, oid.get(0), oid.get(1), true),
//                                SNMP_EXECUTOR
//                        ));
//                    }
//                }
//            }
//        }
//
//        // 结果
//        BigDecimal ipv4Sum = CompletableFuture.allOf(v4Futures.toArray(new CompletableFuture[0]))
//                .thenApply(v -> v4Futures.stream()
//                        .map(CompletableFuture::join)
//                        .filter(Objects::nonNull)
//                        .map(map -> new BigDecimal(map.get("in")).add(new BigDecimal(map.get("out"))))
//                        .reduce(BigDecimal.ZERO, BigDecimal::add)
//                ).join();
//
//        BigDecimal ipv6Sum = CompletableFuture.allOf(v6Futures.toArray(new CompletableFuture[0]))
//                .thenApply(v -> v6Futures.stream()
//                        .map(CompletableFuture::join)
//                        .filter(Objects::nonNull)
//                        .map(map -> new BigDecimal(map.get("in")).add(new BigDecimal(map.get("out"))))
//                        .reduce(BigDecimal.ZERO, BigDecimal::add)
//                ).join();
//
//        // 查询
//        Map<String, Object> params = new HashMap<>();
//        params.put("startOfDay", historyDate);
//        params.put("endOfDay", date);
//        params.put("orderBy","addTime");
//        params.put("orderType","desc");
//        List<FlowStatistics> historyStats = flowStatisticsService.selectObjByMap(params);    //flux
//
//        FlowStatistics currentStat = new FlowStatistics();
//        currentStat.setAddTime(date);
//        currentStat.setIpv4Sum(ipv4Sum);
//        currentStat.setIpv6Sum(ipv6Sum);
//        flowStatisticsService.save1(currentStat);
//
//        // 6. 数据处理计算
//        if (!historyStats.isEmpty()) {
//            FlowStatistics historyStat = historyStats.get(0);
//            BigDecimal ipv4Delta = ipv4Sum.subtract(historyStat.getIpv4Sum());
//            BigDecimal ipv6Delta = ipv6Sum.subtract(historyStat.getIpv6Sum());
//
//            // 7. 封装数据处理逻辑
//            if (ipv4Delta.compareTo(BigDecimal.ZERO) >= 0 &&
//                    ipv6Delta.compareTo(BigDecimal.ZERO) >= 0) {
//
//                BigDecimal totalDelta = ipv4Delta.add(ipv6Delta);
//                if (totalDelta.compareTo(BigDecimal.ZERO) != 0) {
//                    currentStat.setIpv6Rate(ipv6Delta.divide(totalDelta, DECIMAL_SCALE, ROUNDING_MODE));
//                }
//                // 单位转换(MB)
//                currentStat.setIpv4Sum(ipv4Delta.divide(BigDecimal.valueOf(1000000), DECIMAL_SCALE, ROUNDING_MODE));
//                currentStat.setIpv6Sum(ipv6Delta.divide(BigDecimal.valueOf(1000000), DECIMAL_SCALE, ROUNDING_MODE));
//            }else {
//                //处理负值
//                Map map=new HashMap();
//                map.put("orderBy","addTime");
//                map.put("orderType","desc");
//                currentStat = flowStatisticsService.selectObjByMap1(map).get(0);
//                currentStat.setAddTime(date);
//            }
//            flowStatisticsService.save(currentStat);
//        }
//
//        log.info("流量采集完成，耗时：{}ms", System.currentTimeMillis() - start);
//        return ResponseUtil.ok();
//    }

    // SNMP请求方法
    private Map<String, String> execSNMP(FluxConfig config, String inOid, String outOid, boolean isV6) {
        try {
            String ip = isV6 ?
                    (config.getIpv4() == null ? config.getIpv6() : config.getIpv4()) :
                    (config.getIpv4() != null ? config.getIpv4() : config.getIpv6());

            if (StringUtils.isBlank(ip)) return null;

            SNMPV3Params params = new SNMPV3Params.Builder()
                    .version(config.getVersion())
                    .host(ip)
                    .community(config.getCommunity())
                    .port(config.getPort())
                    .build();

            log.info("{}流量采集开始，目标：{}", isV6 ? "IPv6" : "IPv4", ip);
            String result = SNMPv3Request.getTraffic(params, inOid, outOid);

            if (StringUtils.isNotEmpty(result)) {
                log.info("{}流量采集结果：{}", isV6 ? "IPv6" : "IPv4", result);
                return JSONObject.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            log.error("SNMP请求异常：{}", e.getMessage());
        }
        return null;
    }
}
