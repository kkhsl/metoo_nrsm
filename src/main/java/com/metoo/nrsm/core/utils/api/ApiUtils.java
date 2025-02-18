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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Component
public class ApiUtils {

    private final static String URL = "http://59.52.34.196:6001/apisix/blade-ipv6/industryUnit";

    // 测试异常
//    private final static String URL = "http://59.52.34.196:6002/apisix/blade-ipv6/industryUnit";
//      private final static String URL = "http://127.0.0.1:8930/api/nrsm/traffic/data";

    @Autowired
    private ApiService apiService;

    public void sendThirdPartyApiRequests(List<UnitVO> unitVos) {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 设定线程池大小
        List<Callable<String>> tasks = new ArrayList<>();

        for (UnitVO unitVO : unitVos) {
            tasks.add(() -> {
                try {
                    String data = JSONObject.toJSONString(unitVO);
                    byte[] utf8Bytes = data.getBytes(StandardCharsets.UTF_8);
                    String utf8String = new String(utf8Bytes, StandardCharsets.UTF_8);
                    JindustryUnitRequest jindustryUnitRequest = new JindustryUnitRequest();
                    jindustryUnitRequest.setData(utf8String);
                    jindustryUnitRequest.setNonce(UUID.randomUUID().toString());
                    DateTools dateTools = new DateTools();
                    jindustryUnitRequest.setTimestamp(dateTools.getTimestamp());
                    log.info("监管平台 ======================= " + jindustryUnitRequest.getData());
                    return apiService.callThirdPartyApiT("http://59.52.34.196:6001/apisix/blade-ipv6/industryUnit", jindustryUnitRequest);
                } catch (Exception e) {
                    log.error("调用API时发生异常: ", e);  // 优化异常处理，记录详细日志
                    return null;
                }
            });
        }

        try {
            // 使用线程池执行所有任务
            List<Future<String>> results = executorService.invokeAll(tasks);
            for (Future<String> result : results) {
                if (result.isDone()) {
                    log.info("Traffix api ===============" + result.get());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown(); // 关闭线程池
        }
    }

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
                log.info("单位信息：" + jindustryUnitRequest.getData());

                apiService.callThirdPartyApiTT(URL,
                        jindustryUnitRequest);
//                apiService.callThirdPartyApiTRetries(URL,
//                        jindustryUnitRequest);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void monitorApi(List<UnitVO> unitVos){
        try {
            for (UnitVO unitVo : unitVos) {
                apiService.callDataLogToSelf(JSON.toJSONString(unitVo));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
