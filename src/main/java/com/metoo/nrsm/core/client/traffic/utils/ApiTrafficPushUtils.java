package com.metoo.nrsm.core.client.traffic.utils;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.core.utils.api.JindustryUnitRequest;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.UnitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ApiTrafficPushUtils {

    private final static String URL = "http://172.18.242.250:30012/apisix/blade-ipv6/industryUnit";

    @Autowired
    private ApiService apiService;

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
                log.info("单位信息：" + jindustryUnitRequest.getData());

                apiService.callThirdPartyApiTT(URL,
                        jindustryUnitRequest);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
