package com.metoo.nrsm.core.traffic.pull;

import com.metoo.nrsm.core.config.ssh.utils.DateUtils;
import com.metoo.nrsm.core.manager.utils.SseManager;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.ITrafficService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.traffic.push.utils.TrafficPushApiUtils;
import com.metoo.nrsm.core.traffic.utils.TrafficUtils;
import com.metoo.nrsm.core.traffic.utils.UnitFlowUtils;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.UnitVO;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Traffic;
import com.metoo.nrsm.entity.Unit;
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
    private IUnitService unitService;
    @Autowired
    private TrafficPushApiUtils trafficPushApiUtils;
    @Autowired
    private UnitFlowUtils unitFlowUtils;

    @Value("${task.switch.traffic.api.is-open}")
    private boolean trafficApi;

    private final ReentrantLock trafficApiLock = new ReentrantLock();

    @Autowired
    private TrafficUtils trafficUtils;

    SseManager sseManager=new SseManager();

    @Scheduled(cron = "0 */5 * * * ?")
    public void pullTraffic(){
        if (trafficApi) {
            if (trafficApiLock.tryLock()) {

                log.info("流量采集任务开始");
                try {
                    final String TASK_TYPE = "TRAFFIC"; // 任务类型标识
                    Long time1 = System.currentTimeMillis();

                    sseManager.sendLogToAll(TASK_TYPE, "流量采集任务开始");

                    String time = DateUtils.getDateTimeWithZeroSeconds(new Date());
                    LocalDateTime baseTime = TimeUtils.getNow();
                    String currentTime = TimeUtils.format(TimeUtils.clearSecondAndNano(baseTime));
                    String currentTimestamp = String.valueOf(currentTime);

                    String fiveMinutesBefore = TimeUtils.format(TimeUtils.getFiveMinutesBefore(baseTime));
                    Map params = new HashMap();
                    params.put("hidden", false);
                    List<FlowUnit> flowUnits = flowUnitService.selectObjByMap(params);
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
//
                        String ipv4Flow = String.valueOf(dataMap.getOrDefault("ipv4Flow", "0.0"));
                        String ipv6Flow = String.valueOf(dataMap.getOrDefault("ipv6Flow", "0.0"));
//
//                        Random rand = new Random();
//                        String ipv4Flow = String.valueOf(rand.nextInt(5) + 0.1);
//                        String ipv6Flow = String.valueOf(rand.nextInt(2) + 0.1);

//
                        flowUnit.setVfourFlow(ipv4Flow);
                        flowUnit.setVsixFlow(ipv6Flow);

                        log.info("成功保存流量数据: 单位: {}, IPv4: {}, IPv6: {}", flowUnit.getUnitName(),
                                flowUnit.getVfourFlow(), flowUnit.getVsixFlow());
                    }

                    List<UnitVO> unitVos = getUnitVos(time, currentTimestamp, flowUnits);

                    if (unitVos.isEmpty()) {
                        log.info("未找到单位数据");
                        return;
                    }

                    // 入库单位流量
                    unitFlowUtils.saveUnitHourFlowStats(flowUnits, baseTime);

//                    log.info("流量分析 API");
//                    trafficUtils.callApi(unitVos);

                    String execTime = "流量采集时间:" + DateTools.measureExecutionTime(System.currentTimeMillis() - time1);
                    log.info(execTime);
                    sseManager.sendLogToAll(TASK_TYPE, execTime);
                    sseManager.sendLogToAll(TASK_TYPE, "流量采集任务完成");

                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("流量采集任务异常: {}", e.getMessage());
                }finally {
                    if(trafficApiLock != null){
                        trafficApiLock.unlock();
                        log.info("流量采集任务结束");
                    }
                }
            }
        }
    }

    // 获取单位信息并转换为UnitVO的列表
    private List<UnitVO> getUnitVos(String time, String currentTimestamp, List<FlowUnit> unitList) {
        Map<String, Object> params = new HashMap<>();
        params.put("hidden", false);
        List<UnitVO> unitVos = new ArrayList<>();
        if (!unitList.isEmpty()) {
            for (FlowUnit unit : unitList) {
                String unitName = unit.getUnitName();
                if(unit.getUnitId() != null){
                    Unit unit1 = unitService.selectObjById(unit.getUnitId());
                    if(unit1 != null){
                        unitName = unit1.getUnitName();
                    }
                }
                UnitVO unitVO = new UnitVO(
                        unitName, unit.getDepartment(), unit.getArea(),
                        unit.getCity(), time, currentTimestamp,
                        unit.getVfourFlow(), unit.getVsixFlow(), unit.getBroadband_Account()
                );
                unitVos.add(unitVO);
            }
        }
        return unitVos;
    }


}
