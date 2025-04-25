package com.metoo.nrsm.core.wsapi;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.wsapi.utils.NoticeWebsocketResp;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import com.metoo.nrsm.entity.FlowStatistics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

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

    /**
     *
     * @param requestParams
     * @return
     */
    @GetMapping
    public NoticeWebsocketResp flow(@RequestParam(value = "requestParams") String requestParams){
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        if(requestParams != null && !requestParams.isEmpty()){
            Map param = JSONObject.parseObject(requestParams, Map.class);
            String sessionId = (String) param.get("sessionId");
            // 1.查询指定时间的流量数据
            Map params = new HashMap();
            params.put("startOfDay", DateTools.getStartOfDay());
            params.put("endOfDay", DateTools.getEndOfDay());
            List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);

            // 补零前端显示
            // 2. 生成所有 5 分钟间隔的时间点
            List<Date> allTimeSlots = generate5MinuteTimeSlots();

            // 3. 补全缺失的数据
            List<FlowStatistics> completeData = new ArrayList<>();
            for (Date timeSlot : allTimeSlots) {
                Optional<FlowStatistics> matchingData = flowStatisticsList.stream()
                        .filter(data -> DateUtils.truncate(data.getAddTime(), Calendar.MINUTE).equals(DateUtils.truncate(timeSlot, Calendar.MINUTE)))
                        .findFirst();

                if (matchingData.isPresent()) {
                    completeData.add(matchingData.get());
                } else {
                    completeData.add(createDefaultFlowStatistics(timeSlot));
                }
            }

            if (completeData.size() > 0) {
                rep.setNoticeType("301");
                rep.setNoticeStatus(1);
                rep.setNoticeInfo(completeData);
                this.redisResponseUtils.syncRedis(sessionId, completeData, 301);
            }
        }
        rep.setNoticeStatus(0);
        return rep;
    }

    // 生成当天所有 5 分钟间隔的时间点
    private List<Date> generate5MinuteTimeSlots() {
        List<Date> timeSlots = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateTools.getStartOfDay());
        // 清除秒和毫秒
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        while (calendar.getTime().before(DateTools.getEndOfDay())) {
            timeSlots.add(calendar.getTime());
            calendar.add(Calendar.MINUTE, 5);
        }

        return timeSlots;
    }

    // 创建默认的流量数据（IPv4/IPv6 设为 0）
    private FlowStatistics createDefaultFlowStatistics(Date time) {
        FlowStatistics defaultData = new FlowStatistics();
        defaultData.setAddTime(time);
        defaultData.setIpv4(BigDecimal.ZERO);
        defaultData.setIpv6(BigDecimal.ZERO);
        defaultData.setIpv6Rate(BigDecimal.ZERO);
        return defaultData;
    }

}
