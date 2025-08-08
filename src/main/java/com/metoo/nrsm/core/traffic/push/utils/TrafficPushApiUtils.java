package com.metoo.nrsm.core.traffic.push.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.manager.utils.SseManager;
import com.metoo.nrsm.core.utils.api.JindustryUnitRequest;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.UnitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Component
public class TrafficPushApiUtils {

    @Autowired
    private TrafficPushApiService trafficPushApiService;

    @Value("${traffix.push.manager.api.url}")
    private  String managerUrl;

    public String pushTrafficUnit(UnitVO unitVO) {
        StringBuilder logResult = new StringBuilder(); // 存储所有日志信息

        try {
            String data = JSONObject.toJSONString(unitVO);
            byte[] utf8Bytes = data.getBytes(StandardCharsets.UTF_8);
            String utf8String = new String(utf8Bytes, StandardCharsets.UTF_8);

            JindustryUnitRequest jindustryUnitRequest = new JindustryUnitRequest();
            jindustryUnitRequest.setData(utf8String);
            jindustryUnitRequest.setNonce(UUID.randomUUID().toString());
            DateTools dateTools = new DateTools();
            jindustryUnitRequest.setTimestamp(dateTools.getTimestamp());

            String apiResponse = trafficPushApiService.callThirdPartyApiTT(managerUrl, jindustryUnitRequest);

            // 解析 API 返回的 JSON，简化结果
            String result = parseApiResult(apiResponse);

            // 构造日志信息
            String logMessage = String.format(
                    "流量监管平台 | 推送单位：%s | API结果：%s | 流量 vfourFlow:%s vSixFlow:%s",
                    unitVO.getUnitName(),
                    result,
                    unitVO.getVfourFlow(),
                    unitVO.getVsixFlow()
            );

            log.info(logMessage); // 打印日志（可选）
            logResult.append(logMessage).append("\n"); // 存储日志

        } catch (Exception e) {
            String errorMessage = String.format("流量监管平台 | 推送单位 %s 时发生异常：%s", unitVO.getUnitName(), e.getMessage());
            log.error(errorMessage, e);
            logResult.append(errorMessage).append("\n"); // 记录错误信息
        }
        return logResult.toString(); // 返回所有日志
    }

    /**
     * 解析 API 返回的 JSON，返回简化后的结果
     */
    private String parseApiResult(String apiResponse) {
        try {
            JSONObject json = JSONObject.parseObject(apiResponse);
            int code = json.getIntValue("code");
            boolean success = json.getBooleanValue("success");
            String msg = json.getString("msg");

            if (code == 200 && success) {
                return "推送成功"; // 符合条件时返回简化结果
            } else {
                return msg; // 其他情况返回原始消息
            }
        } catch (Exception e) {
            return apiResponse; // 解析失败时返回原始响应
        }
    }



    // 创建SSE管理器，获取全局会话，指定推送流量调用日志
    SseManager sseManager = new SseManager();

    // 推送数据到管理平台 manager platform
    // 增加推送流量日志SSE
    public void pushTrafficManagerPlatform(List<UnitVO> unitVOList){
        if(unitVOList.size() > 0){
            for (UnitVO unitVO : unitVOList) {
                String log = pushTrafficUnit(unitVO);
                sseManager.sendLogToAll("TRAFFIC-LOG", log);
            }
        }
    }

    public void monitorApi(List<UnitVO> unitVos) {
        try {
            for (UnitVO unitVo : unitVos) {
                trafficPushApiService.callSelf(JSON.toJSONString(unitVo));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
