package com.metoo.nrsm.core.manager.statis;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.statis.vo.EchartLineData;
import com.metoo.nrsm.core.manager.statis.vo.EchartLineMonitorData;
import com.metoo.nrsm.core.manager.statis.vo.FlowRadioData;
import com.metoo.nrsm.core.manager.statis.vo.FlowRadioDataExport;
import com.metoo.nrsm.core.service.IUnitFlowStatisFrontService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.poi.ExcelUtils;
import com.metoo.nrsm.core.utils.statis.FlowStatsUtils;
import com.metoo.nrsm.core.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Comparator.comparing;


@RequestMapping("/admin/unitFlowStatis")
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
    @ApiOperation("所有单位按时间段导出")
    @GetMapping(value = "/export")
    public Object export(HttpServletResponse response, @RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {

        if (StringUtil.isEmpty(startTime)) {
            return ResponseUtil.badArgument("开始时间不能为空");
        }
        if (StringUtil.isEmpty(endTime)) {
            return ResponseUtil.badArgument("结束时间不能为空");
        }
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy年MM月dd日");
        String chineseStartDate = LocalDate.parse(startTime).format(formatter);
        String chineseEndDate = LocalDate.parse(endTime).format(formatter);
        String titleHead;
        if(chineseStartDate.equals(chineseEndDate)){
            titleHead=chineseStartDate;
        }else{
            titleHead=chineseStartDate+"-"+chineseEndDate;
        }
        // 动态表头
        List<String> headers = Arrays.asList("单位/部门", "IPv4流量(G)", "IPv6流量(G)","总流量(G)","IPv6流量占比(%)");
        List<FlowRadioDataExport> dataList = this.flowStatsService.queryStatsByTime(startTime,endTime);
        List<Map<String, Object>> data = new ArrayList<>();
        if(CollUtil.isNotEmpty(dataList)){
            dataList.sort(comparing(FlowRadioDataExport::getIpv6Radio).reversed());
            for (FlowRadioDataExport item : dataList) {
                Map<String, Object> row1 = new HashMap<>();
                row1.put("单位/部门", item.getTitle());
                row1.put("IPv4流量(G)", item.getIpv4());
                row1.put("IPv6流量(G)", item.getIpv6());
                row1.put("总流量(G)", item.getTotal());
                row1.put("IPv6流量占比(%)", item.getIpv6Radio());
                data.add(row1);
            }
        }
        ExcelUtils.exportDynamicHeaderExcel(response,headers,data,"部门流量分析" + DateTools.getCurrentDate(new Date()),titleHead);
        return ResponseUtil.ok();
    }
}
