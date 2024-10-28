package com.metoo.nrsm.core.utils.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.UnitVO;
import groovy.transform.SelfType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ApiUtils {

    @Autowired
    private ApiService apiService;

    public void partyApi(List<UnitVO> unitVos){
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
                log.info("'监管平台 ======================= " + jindustryUnitRequest.getData());
                String result = apiService.callThirdPartyApiT("http://59.52.34.196:6001/apisix/blade-ipv6/industryUnit",
                        jindustryUnitRequest);
                log.info("Traffix api ===============" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void monitorApi(List<UnitVO> unitVos){
        try {
            for (UnitVO unitVo : unitVos) {
                apiService.sendDataToMTO(JSON.toJSONString(unitVo));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
