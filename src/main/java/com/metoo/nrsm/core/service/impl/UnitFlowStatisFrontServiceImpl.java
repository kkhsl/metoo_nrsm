package com.metoo.nrsm.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.metoo.nrsm.core.manager.statis.vo.EchartData;
import com.metoo.nrsm.core.manager.statis.vo.EchartLineData;
import com.metoo.nrsm.core.manager.statis.vo.EchartLineMonitorData;
import com.metoo.nrsm.core.manager.statis.vo.FlowRadioData;
import com.metoo.nrsm.core.mapper.UnitFlowStatsMapper;
import com.metoo.nrsm.core.mapper.UnitHourFlowStatsMapper;
import com.metoo.nrsm.core.service.IUnitFlowStatisFrontService;
import com.metoo.nrsm.core.utils.statis.NumUtils;
import com.metoo.nrsm.entity.UnitFlowStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.metoo.nrsm.core.common.FlowConstants.*;
import static com.metoo.nrsm.core.utils.statis.FlowStatsUtils.buildResult;
@Service
public class UnitFlowStatisFrontServiceImpl implements IUnitFlowStatisFrontService {
    @Resource
    private UnitFlowStatsMapper flowStatsMapper;
    @Resource
    private UnitHourFlowStatsMapper hourFlowStatsMapper;
    @Override
    public List<FlowRadioData> allOrgDayStats(String statsDimension, String filter) {
        return flowStatsMapper.allOrgDayStats(statsDimension,Long.parseLong(filter.replaceAll(StrUtil.DASHED, "")));
    }

    @Override
    public EchartLineMonitorData statsByDimension(String statsDimension, String filter) {
        return buildResult(busiStats(statsDimension,filter));
    }

    public EchartLineData busiStats( String statsDimension, String filter) {
        switch (statsDimension) {
            case STATS_DIMENSION_DAY:
                return busiHourById(filter);
            case STATS_DIMENSION_MONTH:
                return busiDayById(filter);
            case STATS_DIMENSION_YEAR:
                return busiMonthById(filter);
            default:
                break;
        }
        return EchartLineData.builder().build();
    }

    /**
     * 小时维度
     * @param day
     * @return
     */
    public EchartLineData busiHourById(String day) {
        //  实时
        List<FlowRadioData> dataList = hourFlowStatsMapper.orgHour(null,Long.valueOf(day.replace(StrUtil.DASHED, "")));
        if (CollUtil.isNotEmpty(dataList)) {
            return constructData(dataList, STATS_DIMENSION_DAY, day);
        }
        return EchartLineData.builder().build();
    }

    /**
     * 天维度
     * @param month
     * @return
     */
    public EchartLineData busiDayById(String month) {
        // 查询具体行业
        List<FlowRadioData> dataList = flowStatsMapper.busiDay(Integer.valueOf(month.replace(StrUtil.DASHED, "")));
        if (CollUtil.isNotEmpty(dataList)) {
            return constructData(dataList, STATS_DIMENSION_MONTH, month);
        }
        return EchartLineData.builder().build();
    }

    /**
     * 月维度
     * @param year
     * @return
     */
    public EchartLineData busiMonthById( String year) {
        // 查询具体行业
        List<FlowRadioData> dataList = flowStatsMapper.busiMonth(Integer.valueOf(year));
        if (CollUtil.isNotEmpty(dataList)) {
            return constructData(dataList, STATS_DIMENSION_YEAR, year);
        }
        return EchartLineData.builder().build();
    }
    /**
     * 根据部门编码获取单位日、月度、年流量统计
     */
    @Override
    public EchartLineData orgFlowStatsById(Long id, String statsDimension, String filter) {
        switch (statsDimension) {
            case STATS_DIMENSION_DAY:
                return orgHourById(id,filter);
            case STATS_DIMENSION_MONTH:
                return orgDayById(id,filter);
            case STATS_DIMENSION_YEAR:
                return orgMonthById(id,filter);
            default:break;
        }
        return EchartLineData.builder().build();
    }

    @Override
    public boolean save(UnitFlowStats stats) {
        try {
            return flowStatsMapper.insert(stats) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 小时统计
     * @param id
     * @param day
     * @return
     */
    public EchartLineData orgHourById(Long id,String day) {
        //  实时的需补充
        List<FlowRadioData> hourData=hourFlowStatsMapper.orgHour(id,Long.parseLong(day.replaceAll(StrUtil.DASHED,"")));
        if (CollUtil.isNotEmpty(hourData)) {
            return constructData(hourData,day);
        }
        return EchartLineData.builder().build();
    }

    /**
     * 天统计
     * @param id
     * @param month
     * @return
     */
    public EchartLineData orgDayById(Long id,String month) {
        // 查询具体单位
        List<UnitFlowStats> dataList=flowStatsMapper.queryList(id,Integer.valueOf(month.replaceAll(StrUtil.DASHED,"")), null,STATS_DIMENSION_MONTH);
        if(CollUtil.isNotEmpty(dataList)){
            // 获取最大值数据
            Map<String,List<UnitFlowStats>> dayData=dataList.stream().collect(Collectors.groupingBy(UnitFlowStats -> UnitFlowStats.getDay().toString()));
            return constructData(dayData,STATS_DIMENSION_MONTH,month);
        }
        return EchartLineData.builder().build();
    }

    /**
     * 月统计
     * @param id
     * @param year
     * @return
     */
    public EchartLineData orgMonthById(Long id,String year) {
        // 查询具体单位
        List<UnitFlowStats> dataList=flowStatsMapper.queryList(id,null, Integer.valueOf(year),STATS_DIMENSION_MONTH);
        if(CollUtil.isNotEmpty(dataList)){
            // 获取最大值数据
            Map<String,List<UnitFlowStats>> monthData=dataList.stream().collect(Collectors.groupingBy(UnitFlowStats -> UnitFlowStats.getMonth().toString()));
            return constructData(monthData,STATS_DIMENSION_YEAR,year);
        }
        return EchartLineData.builder().build();
    }

    /**
     * 组织数据
     *
     * @param statsData
     * @return
     */
    public EchartLineData constructData( List<FlowRadioData> statsData,String filter) {
        EchartLineData result = EchartLineData.builder().build();
        List<EchartData> value = CollUtil.newArrayList();
        List<Integer> allTitle= NumUtils.getHoursOfDayArray(filter);
        result.setTitle(allTitle.stream().map(o -> DateUtil.format(DateUtil.parse(o+"", "yyyyMMddHH"), "yyyy-MM-dd HH:00")).collect(Collectors.toList()));
        // ipv4数据
        EchartData ipv4Data = EchartData.builder().name(IPV4).build();
        List<Double> ipv4ydata = CollUtil.newArrayList();
        // ipv6数据
        EchartData ipv6Data = EchartData.builder().name(IPV6).build();
        List<Double> ipV6yData = CollUtil.newArrayList();
        // ipv6数据
        EchartData ipv6RadioData = EchartData.builder().name(IPV6RADIO).build();
        List<Double> ipv6RadioYData = CollUtil.newArrayList();
        Map<String,List<FlowRadioData>> mapDataGroup=statsData.stream().collect(Collectors.groupingBy(temp -> temp.getTitle()));
        for (Integer integer : allTitle) {
            List<FlowRadioData> tempData = mapDataGroup.get(integer+"");
            if (CollUtil.isNotEmpty(tempData)) {
                ipv4ydata.add(tempData.get(0).getIpv4());
                ipV6yData.add(tempData.get(0).getIpv6());
                ipv6RadioYData.add(tempData.get(0).getIpv6Radio());
            }else{
                ipv4ydata.add(0D);
                ipV6yData.add(0D);
                ipv6RadioYData.add(0D);
            }
        }
        ipv4Data.setData(ipv4ydata);
        value.add(ipv4Data);
        // ipv6数据
        ipv6Data.setData(ipV6yData);
        value.add(ipv6Data);
        // ipv6Radio数据
        ipv6RadioData.setData(ipv6RadioYData);
        value.add(ipv6RadioData);
        result.setValue(value);
        return result;
    }

    /**
     * 组织数据
     * @param statsData
     * @return
     */
    public EchartLineData constructData(Map<String,List<UnitFlowStats>> statsData,String statsDimension,String filter){
        EchartLineData result= EchartLineData.builder().build();
        List<EchartData> value =CollUtil.newArrayList();
        // ipv4数据
        EchartData ipv4Data = EchartData.builder().name(IPV4).build();
        List<Double> ipv4ydata =CollUtil.newArrayList();
        List<Double> ipV6yData=CollUtil.newArrayList();
        List<Integer> allTitle=CollUtil.newArrayList();
        // ipv6数据
        EchartData ipv6Data = EchartData.builder().name(IPV6).build();
        // ipv6数据
        EchartData ipv6RadioData = EchartData.builder().name(IPV6RADIO).build();
        List<Double> ipv6RadioYData = CollUtil.newArrayList();
        switch (statsDimension) {
            case STATS_DIMENSION_DAY:
                // 到小时维度
                // 获取所有小时的维度数据
                allTitle= NumUtils.getHoursOfDayArray(filter);
                result.setTitle(allTitle.stream().map(o -> DateUtil.format(DateUtil.parse(o+"", "yyyyMMddHH"), "yyyy-MM-dd HH:00")).collect(Collectors.toList()));
                break;
            case STATS_DIMENSION_MONTH:
                // 到天维度
                allTitle= NumUtils.getDaysOfMonthArray(filter);
                result.setTitle(allTitle.stream().map(o -> DateUtil.format(DateUtil.parse(o+"", "yyyyMMdd"), "yyyy-MM-dd")).collect(Collectors.toList()));
                break;
            case STATS_DIMENSION_YEAR:
                allTitle=NumUtils.getAllMonth(filter);
                result.setTitle(allTitle.stream().map(o -> DateUtil.format(DateUtil.parse(o+"", "yyyyMM"), "yyyy-MM")).collect(Collectors.toList()));
                break;
            default:
                break;
        }
        for (Integer one : allTitle) {
            List<UnitFlowStats> tempData = statsData.get(one+"");
            if (CollUtil.isNotEmpty(tempData)) {
                Double ipv4=tempData.get(0).getIpv4();
                Double ipv6=tempData.get(0).getIpv4();
                Double ipv6Radio = NumUtils.divUtil(NumberUtil.mul(ipv6, Double.valueOf(100d)), (ipv4 + ipv6));
                ipv4ydata.add(ipv4);
                ipV6yData.add(ipv6);
                ipv6RadioYData.add(ipv6Radio);
            }else{
                ipv4ydata.add(0D);
                ipV6yData.add(0D);
                ipv6RadioYData.add(0D);
            }
        }
        ipv4Data.setData(ipv4ydata);
        value.add(ipv4Data);
        // ipv6数据
        ipv6Data.setData(ipV6yData);
        value.add(ipv6Data);
        // ipv6Radio数据
        ipv6RadioData.setData(ipv6RadioYData);
        value.add(ipv6RadioData);
        result.setValue(value);
        return result;
    }

    /**
     * 组织数据
     *
     * @param statsData
     * @return
     */
    public EchartLineData constructData(List<FlowRadioData> statsData, String statsDimension, String filter) {
        EchartLineData result = EchartLineData.builder().build();
        List<EchartData> value = CollUtil.newArrayList();
        // ipv4数据
        EchartData ipv4Data = EchartData.builder().name(IPV4).build();
        List<Double> ipv4ydata = CollUtil.newArrayList();
        List<Double> ipV6yData = CollUtil.newArrayList();
        List<Double> ipV6RadioData = CollUtil.newArrayList();
        List<Integer> allTitle = CollUtil.newArrayList();
        // ipv6数据
        EchartData ipv6Data = EchartData.builder().name(IPV6).build();
        // ipv6radio数据
        EchartData ipv6RadioData = EchartData.builder().name(IPV6RADIO).build();
        Map<String, List<FlowRadioData>> mapDataGroup = statsData.stream().collect(Collectors.groupingBy(FlowRadioData::getTitle));
        switch (statsDimension) {
            case STATS_DIMENSION_DAY:
                // 到小时维度
                // 获取所有小时的维度数据
                allTitle = NumUtils.getHoursOfDayArray(filter);
                result.setTitle(allTitle.stream().map(o -> DateUtil.format(DateUtil.parse(o + "", "yyyyMMddHH"), "yyyy-MM-dd HH:00")).collect(Collectors.toList()));
                break;
            case STATS_DIMENSION_MONTH:
                // 到天维度
                allTitle = NumUtils.getDaysOfMonthArray(filter);
                result.setTitle(allTitle.stream().map(o -> DateUtil.format(DateUtil.parse(o + "", "yyyyMMdd"), "yyyy-MM-dd")).collect(Collectors.toList()));
                break;
            case STATS_DIMENSION_YEAR:
                // 获取最大值数据
                allTitle = NumUtils.getAllMonth(filter);
                result.setTitle(allTitle.stream().map(o -> DateUtil.format(DateUtil.parse(o+"", "yyyyMM"), "yyyy-MM")).collect(Collectors.toList()));
                break;
            default:
                break;
        }
        for (Integer one : allTitle) {
            List<FlowRadioData> tempData = mapDataGroup.get(one + "");
            if (CollUtil.isNotEmpty(tempData)) {
                ipv4ydata.add(tempData.get(0).getIpv4());
                ipV6RadioData.add(tempData.get(0).getIpv6Radio());
                ipV6yData.add(tempData.get(0).getIpv6());
            } else {
                ipv4ydata.add(0D);
                ipV6yData.add(0D);
                ipV6RadioData.add(0D);
            }
        }
        ipv4Data.setData(ipv4ydata);
        value.add(ipv4Data);
        // ipv6数据
        ipv6Data.setData(ipV6yData);
        value.add(ipv6Data);
        // ipv6数据
        ipv6RadioData.setData(ipV6RadioData);
        value.add(ipv6RadioData);
        result.setValue(value);
        return result;
    }
}
