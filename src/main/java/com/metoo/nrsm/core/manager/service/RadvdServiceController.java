package com.metoo.nrsm.core.manager.service;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IRadvdService;
import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.manager.SmartServiceManager;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Radvd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/admin/service")
public class RadvdServiceController {

    @Autowired
    private IRadvdService radvdService;

    private final static String SERVICENAME = "radvd";

    @PostMapping("/control/{action}")
    public Result controlService(@PathVariable String action) {
        try {
            List<Radvd> radvdList = this.radvdService.selectObjByMap(Collections.emptyMap());
            if(radvdList.size() <= 0){
                return ResponseUtil.badArgument("请至少增加一条IPv6前缀!");
            }
            SmartServiceManager serviceManager = new SmartServiceManager();
            ServiceInfo currentStatus = executeAction(serviceManager, "status");
            if (("stop".equals(action) && !currentStatus.isActive()) ||
                    ("start".equals(action) && currentStatus.isActive())) {
                return ResponseUtil.ok(currentStatus);
            }
            ServiceInfo serviceInfo = executeAction(serviceManager, action);
            return ResponseUtil.ok(serviceInfo);
        } catch (ServiceOperationException e) {
            return ResponseUtil.error("操作失败");
        }
    }

    private ServiceInfo executeAction(SmartServiceManager serviceManager, String action) throws ServiceOperationException {
        switch (action.toLowerCase()) {
            case "start":
                return serviceManager.startService(SERVICENAME);
            case "stop":
                return serviceManager.stopService(SERVICENAME);
            case "restart":
                return serviceManager.restartService(SERVICENAME);
            case "status":
                return serviceManager.getStatus(SERVICENAME);
            default:
                throw new ServiceOperationException("未知操作: " + action);
        }
    }

}
