package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IDnsFilterService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.DnsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


@RequestMapping("/admin/dnsFilter")
@RestController
public class DnsFilterManagerController {

    @Autowired
    private IDnsFilterService dnsFilterService;
    @Value("${ssh.hostname}")
    private String host;
    @Value("${ssh.port}")
    private int port = 22;
    @Value("${ssh.username}")
    private String username;
    @Value("${ssh.password}")
    private String password;

    @GetMapping("/list")
    public Result list() {
        List<DnsFilter> dnsFilters = dnsFilterService.selectAll(Collections.emptyMap());
        return ResponseUtil.ok(dnsFilters);
    }


    @GetMapping("/selectDNSFilter")
    public Result selectDNSFilter(@RequestParam String id) {
        DnsFilter dnsFilter = dnsFilterService.updateDNSFilter(Long.parseLong(id));
        return ResponseUtil.ok(dnsFilter);
    }


    @PostMapping("/saveDNSFilter")
    private Result saveDNSFilter(@RequestBody DnsFilter instance) {
        boolean flag = this.dnsFilterService.saveDnsFilter(instance);
        if (flag) {
            try {
                if (restart()) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("启动失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseUtil.error();
    }

    @DeleteMapping("/deleteDNSFilter")
    private Result deleteDNSFilter(@RequestParam String ids) {
        boolean flag = dnsFilterService.deleteDnsFilter(ids);
        if (flag) {
            try {
                if (restart()) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("启动失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseUtil.error("重复删除");
    }


    @GetMapping("/toggleDnsFilter")
    public Result toggleDnsFilter(@RequestParam String id,@RequestParam boolean enable) {
        boolean flag = dnsFilterService.toggleDnsFilter(Long.parseLong(id), enable);
        if (flag) {
            try {
                if (restart()) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("启动失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseUtil.error();
    }

    @GetMapping("/restart")
    public Boolean restart() throws Exception {
        return dnsFilterService.start();
    }



}
