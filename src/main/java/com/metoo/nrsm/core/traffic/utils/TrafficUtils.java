package com.metoo.nrsm.core.traffic.utils;

import com.metoo.nrsm.core.config.ssh.utils.DateUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.traffic.factory.GatherFactory;
import com.metoo.nrsm.core.traffic.push.utils.ApiTrafficPushYingTanUtils;
import com.metoo.nrsm.core.traffic.push.utils.TrafficPushApiUtils;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.vo.UnitVO;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Unit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TrafficUtils {

    @Autowired
    private IUnitService unitService;
    @Autowired
    private IFlowUnitService flowUnitService;
    @Autowired
    private TrafficPushApiUtils trafficPushApiUtils;
    @Autowired
    private ApiTrafficPushYingTanUtils apiTrafficPushYingTanUtils;


    public void pushTraffic() {

        log.info("设置时间");
        String time = DateUtils.getDateTimeWithZeroSeconds(new Date());
        long currentTime = DateUtils.convertDateStringToTimestamp(time, "yyyy-MM-dd HH:mm:ss");
        String currentTimestamp = String.valueOf(currentTime);

        log.info("获取流量数据，并写入单位");
        executeGather();

        List<UnitVO> unitVos = getUnitVos(time, currentTimestamp);

        if (unitVos.isEmpty()) {
            log.info("未找到单位数据");
            return;
        }

        log.info("调用api");
        callApi(unitVos);
    }


    // 执行Gather任务的方法
    private void executeGather() {
        try {
            GatherFactory factory = new GatherFactory();
            Gather gather = factory.getGather(Global.TRAFFIC);
            gather.executeMethod();
        } catch (Exception e) {
            log.error("流量采集失败：{}", e.getMessage()); // 提供更多的错误信息
        }
    }

    // 获取单位信息并转换为UnitVO的列表
    private List<UnitVO> getUnitVos(String time, String currentTimestamp) {
        Map<String, Object> params = new HashMap<>();
        params.put("hidden", false);

        List<FlowUnit> unitList = this.flowUnitService.selectObjByMapToMonitor(params);
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


    // 调用API的方法，避免重复代码
    public void callApi(List<UnitVO> unitVos) {
        // 监管平台（信产）
        sendManagerPlatform(unitVos);
        // 鹰潭监管平台
        sendYingTanLocal(unitVos);
        // 监控平台
        sendMonitor(unitVos);
    }

    private void sendManagerPlatform(List<UnitVO> unitVos){
        try {
            trafficPushApiUtils.pushTrafficManagerPlatform(unitVos);
        } catch (Exception e) {
            log.error("推送监管平台失败：{}", e.getMessage());
        }
    }

    // 推送数据到鹰潭本地流量监测平台、非鹰潭推流量注释
    private void sendYingTanLocal(List<UnitVO> unitVos){
        try {
            apiTrafficPushYingTanUtils.trafficPushApi(unitVos);
        } catch (Exception e) {
            log.error("推送鹰潭监管平台失败：{}", e.getMessage());
        }
    }

    private void sendMonitor(List<UnitVO> unitVos){
        try {
            trafficPushApiUtils.monitorApi(unitVos);
        } catch (Exception e) {
            log.error("推送mt监控平台失败：{}", e.getMessage());
        }
    }


}
