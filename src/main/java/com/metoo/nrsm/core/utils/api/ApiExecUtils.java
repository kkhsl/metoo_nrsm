package com.metoo.nrsm.core.utils.api;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.ssh.utils.DateUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.GatherFactory;
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
public class ApiExecUtils {

    @Autowired
    private IFlowUnitService flowUnitService;
    @Autowired
    private ApiUtils apiUtils;

    // 执行Gather任务的方法
    private void executeGather() {
        try {
            GatherFactory factory = new GatherFactory();
            Gather gather = factory.getGather(Global.TRAFFIC);
            gather.executeMethod();
        } catch (Exception e) {
            log.error("Error executing Gather task", e); // 提供更多的错误信息
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
            this.apiUtils.monitorApi(unitVos);
        } catch (Exception e) {
            log.error("Error calling monitor API", e);
        }

        // 监管平台（信产）
//        try {
//            this.apiUtils.partyApi(unitVos);
//        } catch (Exception e) {
//            log.error("Error calling party API", e);
//        }

//        callThirdPartyApiTWithRetry(unitVos);


    }

    public String callThirdPartyApiTWithRetry(List<UnitVO> unitVos) {
        int retries = 3;
        while (retries > 0) {
            try {
                this.apiUtils.partyApi(unitVos);
            } catch (Exception e) {
                log.warn("API 调用失败，剩余重试次数: " + retries, e);
                retries--;
                if (retries == 0) {
                    log.error("API 调用失败，重试次数耗尽", e);
                    throw e;
                }
            }
        }
        return null;  // 默认返回
    }

    public void exec() {

        String time = DateUtils.getDateTimeWithZeroSeconds(new Date());
        long currentTime = DateUtils.convertDateStringToTimestamp(time, "yyyy-MM-dd HH:mm:ss");
        String currentTimestamp = String.valueOf(currentTime);

        executeGather();

        List<UnitVO> unitVos = getUnitVos(time, currentTimestamp);

        if (unitVos.isEmpty()) {
            log.info("No units found to Process.");
            return;
        }

        log.info("FlowUnit list: {}", JSONObject.toJSONString(unitVos));

        callApi(unitVos);
    }

}
