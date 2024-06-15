package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return ResponseUtil.ok(flowStatisticsList);
    }

    @PostMapping("/test")
    public Result flow1(@RequestParam Map params){
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
