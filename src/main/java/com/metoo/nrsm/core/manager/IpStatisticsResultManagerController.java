package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IpStatisticsResultService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.IpStatisticsResult;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/statistics/result")
@RestController
public class IpStatisticsResultManagerController {

    @Autowired
    private IpStatisticsResultService statisticsResultService;

    @GetMapping({"top", "top/{num}"})
    public Result top(@PathVariable(name = "num", required = false) Integer num) {
        List<IpStatisticsResult> ipStatisticsResultList = this.statisticsResultService.selectObjByTop();
        return ResponseUtil.ok(ipStatisticsResultList);
    }
}

