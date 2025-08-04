package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.core.service.IFluxConfigService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.wsapi.utils.Md5Crypt;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FluxConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RestController
@RequestMapping("/admin/flux/config")
@Slf4j
public class FluxConfigManagerController {

    @Autowired
    private IFluxConfigService fluxConfigService;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;

    private static final ExecutorService SNMP_EXECUTOR = Executors.newFixedThreadPool(20);


    @GetMapping("/flow2")
    public void flow2() {
        Map params = new HashMap();
        params.put("startOfDay", DateTools.getStartOfDay());
        params.put("endOfDay", DateTools.getEndOfDay());
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);

        // 2. 生成所有 5 分钟间隔的时间点
        List<Date> allTimeSlots = generate5MinuteTimeSlots();

        // 3. 补全缺失的数据
        List<FlowStatistics> completeData = new ArrayList<>();
        for (Date timeSlot : allTimeSlots) {
            Optional<FlowStatistics> matchingData = flowStatisticsList.stream()
                    .filter(data -> DateUtils.truncate(data.getAddTime(), Calendar.MINUTE).equals(DateUtils.truncate(timeSlot, Calendar.MINUTE)))
                    .findFirst();

            if (matchingData.isPresent()) {
                completeData.add(matchingData.get());
            } else {
                completeData.add(createDefaultFlowStatistics(timeSlot));
            }
        }
        System.out.println(completeData);
    }

    // 生成当天所有 5 分钟间隔的时间点
    private List<Date> generate5MinuteTimeSlots() {
        List<Date> timeSlots = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateTools.getStartOfDay());
        // 清除秒和毫秒
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        while (calendar.getTime().before(DateTools.getEndOfDay())) {
            timeSlots.add(calendar.getTime());
            calendar.add(Calendar.MINUTE, 5);
        }

        return timeSlots;
    }

    // 创建默认的流量数据（IPv4/IPv6 设为 0）
    private FlowStatistics createDefaultFlowStatistics(Date time) {
        FlowStatistics defaultData = new FlowStatistics();
        defaultData.setAddTime(time);
        defaultData.setIpv4(BigDecimal.ZERO);
        defaultData.setIpv6(BigDecimal.ZERO);
        defaultData.setIpv6Rate(BigDecimal.ZERO);
        return defaultData;
    }

    @GetMapping
    public Result all() {
        List<FluxConfig> fluxConfigList = this.fluxConfigService.selectObjByMap(null);
        if (fluxConfigList.size() > 0) {
            for (FluxConfig fluxConfig : fluxConfigList) {
                List<List<String>> v4_oids = JSONObject.parseObject(fluxConfig.getIpv4Oid(), List.class);
                List<List<String>> v6_oids = JSONObject.parseObject(fluxConfig.getIpv6Oid(), List.class);
                fluxConfig.setIpv4Oids(v4_oids);
                fluxConfig.setIpv6Oids(v6_oids);
            }
        }
        return ResponseUtil.ok(fluxConfigList);
    }

    @PostMapping
    public Object save(@RequestBody FluxConfig fluxConfig) {
        if (fluxConfig == null) {
            return ResponseUtil.badArgument("参数错误");
        }
        if (StringUtils.isNotEmpty(fluxConfig.getIpv4())) {
            boolean flag = Ipv4Util.verifyIp(fluxConfig.getIpv4());
            if (!flag) {
                return ResponseUtil.badArgument("ipv4格式错误");
            }
        }
        if (StringUtils.isNotEmpty(fluxConfig.getIpv6())) {
            boolean flag = Ipv6Util.verifyIpv6(fluxConfig.getIpv6());
            if (!flag) {
                return ResponseUtil.badArgument("ipv6格式错误");
            }
        }

        if (fluxConfig.getIpv4Oids() != null) {
            String ipv4oid = JSONObject.toJSONString(fluxConfig.getIpv4Oids());
            fluxConfig.setIpv4Oid(ipv4oid);
        }

        if (fluxConfig.getIpv6Oids() != null) {
            String ipv6oid = JSONObject.toJSONString(fluxConfig.getIpv6Oids());
            fluxConfig.setIpv6Oid(ipv6oid);
        }

        if (fluxConfig.getId() != null) {
            FluxConfig obj = this.fluxConfigService.selectObjById(fluxConfig.getId());
            fluxConfig.getIpv4Oids().clear();
            fluxConfig.getIpv6Oids().clear();
            boolean flag = Md5Crypt.getDiffrent(obj, fluxConfig);
            if (!flag) {
                fluxConfig.setUpdate(1);
            }
        }
        boolean flag = this.fluxConfigService.save(fluxConfig);
        if (flag) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @DeleteMapping
    public Object delete(String ids) {
        if (ids != null && !ids.equals("")) {
            for (String id : ids.split(",")) {
                Map params = new HashMap();
                params.put("id", Long.parseLong(id));
                List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(params);
                if (fluxConfigs.size() > 0) {
                    FluxConfig fluxConfig = fluxConfigs.get(0);
                    try {
                        boolean i = this.fluxConfigService.delete(Long.parseLong(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return ResponseUtil.badArgument(fluxConfig.getName() + "删除失败");
                    }
                } else {
                    return ResponseUtil.badArgument();
                }
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }

    @GetMapping("/gather")
    @Scheduled(cron = "0 */5 * * * ?")
    public Result gather() {
        long start = System.currentTimeMillis();
        final Date date = new Date();
        final int DECIMAL_SCALE = 10;
        final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

        final Date historyDate = DateUtils.addSeconds(date, -300);

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
                        v4Futures.add(CompletableFuture.supplyAsync(() ->
                                        execSNMP(config, oid.get(0), oid.get(1), false),
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
                        v6Futures.add(CompletableFuture.supplyAsync(() ->
                                        execSNMP(config, oid.get(0), oid.get(1), true),
                                SNMP_EXECUTOR
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

        log.info("流量采集完成，耗时：{}ms", System.currentTimeMillis() - start);
        return ResponseUtil.ok();
    }

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

