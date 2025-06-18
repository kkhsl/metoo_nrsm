package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.service.IDnsRecordService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/monitor/dnsRecord")
@RestController
public class DnsRecordController {

    @Autowired
    private IDnsRecordService recordService;

    @GetMapping("/ipv4TopN")
    @ApiOperation(value = "查询ipv4的topn列表", notes = "查询ipv4的topn列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recordTime", value = "查询日期", dataType = "String"),
            @ApiImplicitParam(name = "topN", value = "topN", dataType = "int")
    })

    public Result ipv4TopN(@RequestParam String recordTime, Integer topN) {
        return ResponseUtil.ok(recordService.ipv4TopN(recordTime, topN));
    }

}
