package com.metoo.nrsm.core.utils.api;

import com.metoo.nrsm.core.client.traffic.utils.ApiTrafficPushUtils;
import com.metoo.nrsm.core.config.ssh.utils.DateUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.GatherFactory;
import com.metoo.nrsm.core.manager.utils.SseManager;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.vo.UnitVO;
import com.metoo.nrsm.entity.FlowUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TrafficPushExecUtils {

    @Autowired
    private IFlowUnitService flowUnitService;
    @Autowired
    private TrafficPushApiUtils trafficPushApiUtils;
    @Autowired
    private ApiTrafficPushUtils apiTrafficPushUtils;


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
                UnitVO unitVO = new UnitVO(
                        unit.getUnitName(), unit.getDepartment(), unit.getArea(),
                        unit.getCity(), time, currentTimestamp,
                        unit.getVfourFlow(), unit.getVsixFlow(), unit.getBroadband_Account()
                );
                unitVos.add(unitVO);
            }
        }

        return unitVos;
    }

    // 调用API的方法，避免重复代码
    private void callApi(List<UnitVO> unitVos) {
        try {
            this.trafficPushApiUtils.monitorApi(unitVos);
        } catch (Exception e) {
            log.error("推送mt监控平台失败：{}", e.getMessage());
        }

        // 监管平台（信产）
        try {
//            this.apiUtils.partyApi(unitVos);
            pushTrafficManagerPlatform(unitVos);
        } catch (Exception e) {
            log.error("推送监管平台失败：{}", e.getMessage());
        }

//        // 推送数据到鹰潭本地流量监测平台
//        try {
//            this.apiTrafficPushUtils.trafficApi(unitVos);
//        } catch (Exception e) {
//            log.error("推送鹰潭监管平台失败：{}", e.getMessage());
//        }
    }


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


    // 创建SSE管理器，获取全局会话，指定推送流量调用日志
    SseManager sseManager = new SseManager();

    // 推送数据到管理平台 manager platform
    // 增加推送流量日志SSE
    public void pushTrafficManagerPlatform(List<UnitVO> unitVOList){
        if(unitVOList.size() > 0){
            for (UnitVO unitVO : unitVOList) {
                String log = trafficPushApiUtils.pushTrafficUnit(unitVO);
                sseManager.sendLogToAll("TRAFFIC-LOG", log);
            }
        }
    }
        

}
