package com.metoo.nrsm.core.wsapi;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.wsapi.utils.NoticeWebsocketResp;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import com.metoo.nrsm.entity.FlowStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-02 11:35
 */
@Slf4j
@RestController
@RequestMapping("ws/api/flow/statistics")
public class FlowStatisticsManagerControllerApi {

    @Autowired
    private IFlowStatisticsService flowStatisticsService;
    @Autowired
    private RedisResponseUtils redisResponseUtils;


    @GetMapping
    public NoticeWebsocketResp flow(@RequestParam(value = "requestParams") String requestParams){
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        if(requestParams != null && !requestParams.isEmpty()){
            Map param = JSONObject.parseObject(requestParams, Map.class);
            String sessionId = (String) param.get("sessionId");
            Map params = new HashMap();
            params.put("startOfDay", DateTools.getStartOfDay());
            params.put("endOfDay", DateTools.getEndOfDay());
            List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
            if (flowStatisticsList.size() > 0) {
                rep.setNoticeType("301");
                rep.setNoticeStatus(1);
                rep.setNoticeInfo(flowStatisticsList);
                this.redisResponseUtils.syncRedis(sessionId, flowStatisticsList, 301);
            }
        }
        rep.setNoticeStatus(0);
        return rep;
    }


}
