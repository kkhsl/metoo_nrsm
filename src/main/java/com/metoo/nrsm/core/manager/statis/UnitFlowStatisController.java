package com.metoo.nrsm.core.manager.statis;

import cn.hutool.core.collection.CollUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.statis.vo.EchartLineData;
import com.metoo.nrsm.core.manager.statis.vo.EchartLineMonitorData;
import com.metoo.nrsm.core.manager.statis.vo.FlowRadioData;
import com.metoo.nrsm.core.service.IUnitFlowStatisFrontService;
import com.metoo.nrsm.core.utils.statis.FlowStatsUtils;
import com.metoo.nrsm.core.vo.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Comparator.comparing;


@RequestMapping("/unitFlowStatis")
@Api(value = "流量分析",tags = {"流量分析"})
@RestController
public class UnitFlowStatisController {

    @Autowired
    private IUnitFlowStatisFrontService flowStatsService;

    @GetMapping("/allOrgDayStats")
    @ApiOperation("所有部门流量情况排名列表-日、月、年")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statsDimension", value = "统计维度", required = true, example = "日：1，月：2，年：3,周：4"),
            @ApiImplicitParam(name = "filter", value = "日期条件", required = true, example = "日：2024-11-23,月：2024-11,年：2024,周：2024-11-23")
    })
    public Result allOrgDayStats( @RequestParam("statsDimension") String statsDimension, @RequestParam("filter") String filter) {
        List<FlowRadioData> result = flowStatsService.allOrgDayStats(statsDimension,filter);
        if(CollUtil.isNotEmpty(result)){
            result.sort(comparing(FlowRadioData::getIpv6Radio).reversed());
        }
        return ResponseUtil.ok(result);
    }
    @GetMapping("/stats")
    @ApiOperation("查询所有部门流量统计出图-日、月、周、年")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statsDimension", value = "统计维度", required = true, example = "日：1，月：2，年：3,周：4"),
            @ApiImplicitParam(name = "filter", value = "日期条件", required = true, example = "日：2024-11-23,月：2024-11,年：2024,周：2024-11-23")
    })
    public Result statsByDimension( @RequestParam("statsDimension") String statsDimension, @RequestParam("filter") String filter) {
        EchartLineMonitorData result = flowStatsService.statsByDimension(statsDimension,filter);
        return ResponseUtil.ok(result);
    }

    @GetMapping("/orgFlowStatsById")
    @ApiOperation("根据部门编码获取单位日、周、月度、年流量统计出图")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024"),
            @ApiImplicitParam(name = "statsDimension", value = "统计维度", required = true, example = "日：1，月：2，年：3,周：4"),
            @ApiImplicitParam(name = "filter", value = "日期条件", required = true, example = "日：2024-11-23,月：2024-11,年：2024,周：2024-11-23")
    })
    public Result orgFlowStatsById(@RequestParam("id") Long id, @RequestParam("statsDimension") String statsDimension, @RequestParam("filter") String filter) {
        EchartLineData result = flowStatsService.orgFlowStatsById(id,statsDimension,filter);
        return ResponseUtil.ok(FlowStatsUtils.buildResult(result));
    }

}
