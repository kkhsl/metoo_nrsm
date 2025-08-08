package com.metoo.nrsm.core.utils.api.netmap;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.traffic.push.utils.TrafficPushApiService;
import com.metoo.nrsm.core.utils.date.DateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class NetmapResultPushApiUtils {

    private final static String URL = "http://182.109.52.105:30012/apisix/blade-xxljob/reformUnit";

    @Autowired
    private TrafficPushApiService trafficPushApiService;

    public void send(Map param) {
            try {
                String data = JSONObject.toJSONString(param);
                NetmapPushRequestParams netmapPushRequestParams = new NetmapPushRequestParams();

                // 编码为 UTF-8
                byte[] utf8Bytes = data.getBytes(StandardCharsets.UTF_8);
                String utf8String = new String(utf8Bytes, StandardCharsets.UTF_8);

                netmapPushRequestParams.setData(utf8String);

                netmapPushRequestParams.setNonce(UUID.randomUUID().toString());
                DateTools dateTools = new DateTools();
                netmapPushRequestParams.setTimestamp(dateTools.getTimestamp());

                String result = trafficPushApiService.callNetmapApi(URL,
                        netmapPushRequestParams);

                log.info("测绘推送监管平台 推送单位：{} 结果：{}", param.get("unitName"), result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

}
