package com.metoo.nrsm.core.traffic.push.utils;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.utils.api.JindustryUnitRequest;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.UnitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ApiTrafficPushYingTanUtils {

    @Value("${traffix.push.yingtan.api.url}")
    private String URL;

    @Autowired
    private TrafficPushApiService trafficPushApiService;

    public void trafficPushApi(List<UnitVO> unitVos) {
        for (UnitVO unitVO : unitVos) {
            try {
                String data = JSONObject.toJSONString(unitVO);
                // 编码为 UTF-8
                byte[] utf8Bytes = data.getBytes(StandardCharsets.UTF_8);

                String utf8String = new String(utf8Bytes, StandardCharsets.UTF_8);
                JindustryUnitRequest jindustryUnitRequest = new JindustryUnitRequest();
                jindustryUnitRequest.setData(utf8String);
                jindustryUnitRequest.setNonce(UUID.randomUUID().toString());
                DateTools dateTools = new DateTools();
                jindustryUnitRequest.setTimestamp(dateTools.getTimestamp());
                String result = trafficPushApiService.callThirdPartyApiTT(URL,
                        jindustryUnitRequest);
                log.info("鹰潭本地流量监管平台 推送单位：{} 结果：{}", unitVO.getUnitName(), result);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("鹰潭本地流量监管平台 推送单位：{} 结果：{}", unitVO.getUnitName(), "推送失败");
            }
        }
    }

}
