package com.metoo.nrsm.core.thirdparty.api.traffic;

import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.ITrafficService;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Traffic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Configuration
public class TrafficPullScheduler {

    @Autowired
    private IFlowUnitService flowUnitService;
    @Autowired
    private TrafficPullApi trafficApiService;
    @Autowired
    private ITrafficService trafficService;

    @Value("${task.switch.traffic.api.is-open}")
    private boolean trafficApi;

    private final ReentrantLock trafficApiLock = new ReentrantLock();

    @Scheduled(cron = "0 */5 * * * ?")
    public void trafficAPI(){
        if (trafficApi) {
            if (trafficApiLock.tryLock()) {
                try {
                    LocalDateTime baseTime = TimeUtils.getNow();
                    String currentTime = TimeUtils.format(TimeUtils.clearSecondAndNano(baseTime));
                    String fiveMinutesBefore = TimeUtils.format(TimeUtils.getFiveMinutesBefore(baseTime));
                    List<FlowUnit> flowUnits = flowUnitService.selectObjByMap(Collections.emptyMap());
                    if (flowUnits == null || flowUnits.isEmpty()) {
                        log.info("未获取到任何单位，跳过调用 NetFlow API。");
                        return;
                    }
                    for (FlowUnit flowUnit : flowUnits) {

                        String unitName = flowUnit.getUnitName();
                        if (unitName == null || unitName.trim().isEmpty()) {
                            log.warn("单位名称为空，跳过该单位的流量查询。");
                            continue;
                        }

                        TrafficPullApi.ApiResponse response = trafficApiService.queryNetFlow(unitName, fiveMinutesBefore, currentTime);

                        if (!response.isSuccess()) {
                            log.error("调用 NetFlow API 失败，单位: {}, 错误信息: {}", unitName, response.getError());
                            continue;
                        }
                        Map<String, Object> dataMap = Optional.ofNullable(response.getData())
                                .map(d -> (Map<String, Object>) d.get("data"))
                                .orElse(null);

                        if (dataMap == null) {
                            log.warn("NetFlow API 返回空数据，单位: {}", unitName);
                            continue;
                        }
                        Traffic traffic = new Traffic();
                        traffic.setUnitName(String.valueOf(dataMap.getOrDefault("name", unitName)));
                        traffic.setVfourFlow(String.valueOf(dataMap.getOrDefault("ipv4Flow", "0.0")));
                        traffic.setVsixFlow(String.valueOf(dataMap.getOrDefault("ipv6Flow", "0.0")));
                        traffic.setAddTime(Date.from(baseTime.atZone(ZoneId.systemDefault()).toInstant()));
                        trafficService.save(traffic);
                        log.info("成功保存流量数据: 单位: {}, IPv4: {}, IPv6: {}", traffic.getUnitName(),
                                traffic.getVfourFlow(), traffic.getVsixFlow());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(trafficApiLock != null){
                        trafficApiLock.unlock();
                    }
                }
            }
        }
    }
}
