package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/admin/flux/statistics")
public class FlowStatisticsManagerController {

    @Autowired
    private IFlowStatisticsService flowStatisticsService;

    @GetMapping
    public Result flow(
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @RequestParam(value = "startOfDay", required = false) Date startOfDay,

            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @RequestParam(value = "endOfDay", required = false) Date endOfDay) {

        try {
            Map<String, Object> params = new HashMap<>();

            // 处理时间参数，确保正确的时间范围
            Date startDate = startOfDay == null ? DateTools.getStartOfDay() : startOfDay;
            Date endDate = endOfDay == null ? DateTools.getEndOfDay() : endOfDay;

            // 确保结束时间不小于开始时间
            if (endDate.before(startDate)) {
                Date temp = startDate;
                startDate = endDate;
                endDate = temp;
            }

            params.put("startOfDay", startDate);
            params.put("endOfDay", endDate);

            // 1. 查询原始统计数据
            List<FlowSummary> flowSummaryList = flowStatisticsService.getFlowSummary(params);

            // 2. 根据搜索时间范围生成时间槽（从startDate到endDate的5分钟间隔）
            List<Date> timeSlots = generateTimeSlotsInRange(startDate, endDate);

            // 3. 补全缺失的数据点
            List<FlowSummary> completeData = completeMissingDataInRange(flowSummaryList, timeSlots);

            return ResponseUtil.ok(completeData);
        } catch (Exception e) {
            return ResponseUtil.fail("流量统计查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据搜索时间范围生成5分钟间隔的时间点
     */
    private List<Date> generateTimeSlotsInRange(Date startDate, Date endDate) {
        List<Date> timeSlots = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // 1. 从起始时间开始（向下取整到最近的5分钟）
        calendar.setTime(startDate);
        int minute = calendar.get(Calendar.MINUTE);
        int mod = minute % 5;
        calendar.add(Calendar.MINUTE, -mod);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date normalizedStart = calendar.getTime();

        // 2. 设置结束时间
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        // 3. 生成时间槽
        calendar.setTime(normalizedStart);
        while (!calendar.getTime().after(endDate)) {
            // 只添加在查询时间范围内的点
            Date timePoint = calendar.getTime();
            if (!timePoint.before(startDate) && !timePoint.after(endDate)) {
                timeSlots.add(timePoint);
            }
            calendar.add(Calendar.MINUTE, 5);
        }

        return timeSlots;
    }

    /**
     * 在指定时间范围内补全缺失数据点
     */
    private List<FlowSummary> completeMissingDataInRange(List<FlowSummary> dataList, List<Date> timeSlots) {
        // 创建时间格式化器，按分钟格式化为"yyyy-MM-dd HH:mm"格式
        SimpleDateFormat minuteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        minuteFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 将数据列表按分钟格式化后存入映射表
        Map<String, FlowSummary> dataMap = new HashMap<>();
        for (FlowSummary data : dataList) {
            String timeKey = minuteFormat.format(data.getCreateTime());
            dataMap.put(timeKey, data);
        }

        List<FlowSummary> completeData = new ArrayList<>();

        // 遍历所有时间槽
        for (Date timeSlot : timeSlots) {
            String timeKey = minuteFormat.format(timeSlot);
            FlowSummary data = dataMap.get(timeKey);

            if (data != null) {
                completeData.add(data);
            } else {
                // 创建默认数据点
                completeData.add(createDefaultFlowStatistics(timeSlot));
            }
        }

        // 按时间排序（确保顺序正确）
        completeData.sort(Comparator.comparing(FlowSummary::getCreateTime));

        return completeData;
    }

    // 创建默认的流量数据
    private FlowSummary createDefaultFlowStatistics(Date time) {
        FlowSummary defaultData = new FlowSummary();
        defaultData.setCreateTime(time);
        defaultData.setIpv4TotalGb(BigDecimal.ZERO);
        defaultData.setIpv6TotalGb(BigDecimal.ZERO);
        defaultData.setIpv6Percentage(0.0);
        return defaultData;
    }

/*    @PostMapping("/test")
    public Result flowTest(@RequestParam Map<String, Object> params) {
        try {
            if (params.get("startOfDay") == null) {
                params.put("startOfDay", DateTools.getStartOfDay());
            }
            if (params.get("endOfDay") == null) {
                params.put("endOfDay", DateTools.getEndOfDay());
            }
            List<FlowSummary> flowSummaryList = flowStatisticsService.getFlowSummary(params);
            return ResponseUtil.ok(mapToFlowStatistics(flowSummaryList));
        } catch (Exception e) {
            return ResponseUtil.fail(e.getMessage());
        }
    }*/
}