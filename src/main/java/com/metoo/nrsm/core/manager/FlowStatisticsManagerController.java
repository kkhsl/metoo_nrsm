package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowStatistics;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-02 9:43
 */
@RestController
@RequestMapping("/admin/flux/statistics")
public class FlowStatisticsManagerController {

    @Autowired
    private IFlowStatisticsService flowStatisticsService;

    @GetMapping
    public Result flow(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
                           @RequestParam(value = "startOfDay", required = false) Date startOfDay,
                       @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
                       @RequestParam(value = "endOfDay", required = false) Date endOfDay){
        Map params = new HashMap();
        if(startOfDay == null || "".equals(startOfDay)){
            params.put("startOfDay", DateTools.getStartOfDay());
        }else{
            params.put("startOfDay", startOfDay);
        }
        if(endOfDay == null || "".equals(endOfDay)){
            params.put("endOfDay", DateTools.getEndOfDay());
        }else{
            params.put("endOfDay", endOfDay);
        }
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);

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

        return ResponseUtil.ok(completeData);
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

    @PostMapping("/test")
    public Result flow(@RequestParam Map params){
        if(params.get("startOfDay") == null || "".equals(params.get("startOfDay") == null)){
            params.put("startOfDay", DateTools.getStartOfDay());
        }
        if(params.get("endOfDay") == null || "".equals(params.get("endOfDay") == null)){
            params.put("endOfDay", DateTools.getEndOfDay());
        }
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
        return ResponseUtil.ok(flowStatisticsList);
    }
}
