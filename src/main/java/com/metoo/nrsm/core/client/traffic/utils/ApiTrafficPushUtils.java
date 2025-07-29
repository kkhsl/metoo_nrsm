package com.metoo.nrsm.core.client.traffic.utils;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.utils.api.TrafficPushApiService;
import com.metoo.nrsm.core.utils.api.JindustryUnitRequest;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.UnitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ApiTrafficPushUtils {

    private final static String URL = "http://182.109.52.105:30012/apisix/blade-ipv6/industryUnit";

    @Autowired
    private TrafficPushApiService trafficPushApiService;

    public void trafficApi(List<UnitVO> unitVos) {
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

                Map params = new HashMap();
                params.put("unitName", unitVO.getUnitName());
                params.put("result", result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(UUID.randomUUID().toString());
    }
}
