package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.FluxDailyRateMapper;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.core.service.IFluxDailyRateService;
import com.metoo.nrsm.core.service.IGradWeightService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FluxDailyRate;
import com.metoo.nrsm.entity.GradeWeight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/admin/flux/Rate")
public class FluxDailyRateManagerController {

    @Autowired
    private IFluxDailyRateService fluxDailyRateService;
    @Autowired
    private IGradWeightService gradWeightService;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;

    @GetMapping
    public Result data(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startTime,
                       @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime){
        Map params = new HashMap();
        params.clear();
        params.put("startOfDay", startTime);
        params.put("endOfDay", endTime);
        List<FluxDailyRate> fluxDailyRates = this.fluxDailyRateService.selectObjByMap(params);
        return ResponseUtil.ok(fluxDailyRates);
    }

    public void gather(){
        Date endOfDay = DateTools.getEndOfDay();
        FluxDailyRate fluxDailyRate = new FluxDailyRate();
        fluxDailyRate.setRate(new BigDecimal(0));
        fluxDailyRate.setAddTime(endOfDay);
        Map params = new HashMap();
        params.clear();
        params.put("startOfDay", DateTools.getStartOfDay());
        params.put("endOfDay", endOfDay);
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
        if(flowStatisticsList.size() > 0){
            BigDecimal sum = flowStatisticsList.stream().filter(e -> e.getIpv6Rate() != null).map(FlowStatistics::getIpv6Rate)
                    .collect(Collectors.toList())
                    .stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            long count = flowStatisticsList.stream().filter(e -> e.getIpv6Rate() != null).map(FlowStatistics::getIpv6Rate)
                    .collect(Collectors.toList())
                    .stream().count();

            if(sum.compareTo(new BigDecimal(0)) >= 1){
                BigDecimal rate = sum.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                fluxDailyRate.setRate(rate);
                GradeWeight gradeWeight = this.gradWeightService.selectObjOne();
                if(gradeWeight != null){
                    if(gradeWeight.getReach() != null && gradeWeight.getReach().compareTo(new BigDecimal(0)) >= 1){
                        if(rate.compareTo(gradeWeight.getReach()) > -1){
                            fluxDailyRate.setFlag(true);
                        }
                    }
                }
            }
        }

        this.fluxDailyRateService.save(fluxDailyRate);
    }
}
