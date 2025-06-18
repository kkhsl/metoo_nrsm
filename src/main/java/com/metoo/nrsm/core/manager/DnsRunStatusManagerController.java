package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IDnsRunStatusService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.DnsRunStatus;
import com.metoo.nrsm.entity.PingIpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 10:15
 */
@RequestMapping("/admin/dns/status")
@RestController
public class DnsRunStatusManagerController {

    @Autowired
    private IDnsRunStatusService dnsRunStatusService;

    @GetMapping
    public Result dns(){
        DnsRunStatus dnsRunStatus = this.dnsRunStatusService.selectOneObj();
        return ResponseUtil.ok(dnsRunStatus);
    }

    @PutMapping
    public Result update(@RequestBody DnsRunStatus instance){
//        DnsRunStatus dnsRunStatus = this.dnsRunStatusService.selectOneObj();
        boolean flag = this.dnsRunStatusService.update(instance);
        if(flag){
            boolean checkdns = this.dnsRunStatusService.checkdns();
            if(!checkdns && instance.isStatus()){
                this.dnsRunStatusService.start();
            }else if(checkdns && !instance.isStatus()){
                this.dnsRunStatusService.stop();
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @GetMapping("/checkdns")
    public Result checkaliveip(){
        boolean flag = this.dnsRunStatusService.checkdns();
        return ResponseUtil.ok(flag);
    }
}
