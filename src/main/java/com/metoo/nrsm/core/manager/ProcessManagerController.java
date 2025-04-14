package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.network.networkconfig.test.checkProcessStatus;
import com.metoo.nrsm.core.service.IUnboundService;
import com.metoo.nrsm.core.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RequestMapping("/admin/process")
@RestController
public class ProcessManagerController {

    @Autowired
    private IUnboundService unboundService;

    @GetMapping("/list")
    private Result select(){
        try {
            // 获取服务状态
            String dhcpdStatus = checkProcessStatus.checkProcessStatus("dhcpd");
            String dhcpd6Status = checkProcessStatus.checkProcessStatus("dhcpd6");
            //String checkaliveipStatus = checkProcessStatus.checkProcessStatus("checkaliveip");
            boolean dnsStatus = unboundService.status(); // DNS状态
            Map<String, String> statusMap = new LinkedHashMap<>(4);
            statusMap.put("dhcpdStatus", convertStringStatus(dhcpdStatus));
            statusMap.put("dhcpd6Status", convertStringStatus(dhcpd6Status));
            //statusMap.put("checkaliveipStatus", convertStringStatus(checkaliveipStatus));
            statusMap.put("dnsStatus", String.valueOf(dnsStatus).toLowerCase());
            return ResponseUtil.ok(statusMap);
        } catch (Exception e) {
            return ResponseUtil.fail("状态诊断失败: " + e.getMessage());
        }
    }

    private String convertStringStatus(String status) {
        return (status != null && status.equalsIgnoreCase("true")) ? "true" : "false";
    }
}
