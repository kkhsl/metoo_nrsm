package com.metoo.nrsm.core.manager.service;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.manager.SmartServiceManager;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;
import com.metoo.nrsm.core.vo.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/service/windows")
public class RadvdServiceWindowsController {

    private final static String SERVICENAME = "radvd";
    private final String host = "192.168.6.102"; // 或者配置文件中获取
    private final int port = 22;
    private final String username = "root";
    private final String password = "Metoo89745000!";
    private final int timeout = 5000; // 超时 5 秒

    @PostMapping("/control/{action}")
    public Result controlService(@PathVariable String action) {
        try {
            SmartServiceManager serviceManager = new SmartServiceManager(host, port, username, password, timeout);
            ServiceInfo currentStatus = executeAction(serviceManager, "status");
            if (("stop".equals(action) && !currentStatus.isActive()) ||
                    ("start".equals(action) && currentStatus.isActive())) {
                return ResponseUtil.ok(currentStatus);
            }
            if("status".equals(action)){
                return ResponseUtil.ok(currentStatus);
            }
            ServiceInfo serviceInfo = executeAction(serviceManager, action);
            return ResponseUtil.ok(serviceInfo);
        } catch (ServiceOperationException e) {
            return ResponseUtil.error("操作失败");
        }
    }

//    @PostMapping("/control/{action}")
//    public ServiceInfo controlService(@PathVariable String action) {
//        try {
//            SmartServiceManager serviceManager = new SmartServiceManager(host, port, username, password, timeout);
//            return executeAction(serviceManager, action);
//        } catch (ServiceOperationException e) {
//        }
//        return null;
//    }

    /**
     * 根据 action 执行相应的服务操作
     */
//    private String executeAction(SmartServiceManager serviceManager, String action) throws ServiceOperationException {
//        switch (action.toLowerCase()) {
//            case "start":
//                serviceManager.startService(SERVICENAME);
//                return "服务 " + SERVICENAME + " 启动成功";
//            case "stop":
//                serviceManager.stopService(SERVICENAME);
//                return "服务 " + SERVICENAME + " 停止成功";
//            case "restart":
//                serviceManager.restartService(SERVICENAME);
//                return "服务 " + SERVICENAME + " 重启成功";
//            case "status":
//                ServiceInfo status = serviceManager.getStatus(SERVICENAME);
//                return "服务 " + SERVICENAME + " 当前状态: " + status;
//            default:
//                throw new ServiceOperationException("未知操作: " + action);
//        }
//    }
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

    /**
     * 启动服务
     */
    @PostMapping("/start")
    public String startService() {
        try {
            SmartServiceManager serviceManager = new SmartServiceManager(host, port, username, password, timeout);
            serviceManager.startService(SERVICENAME);
            return "服务 " + SERVICENAME + " 启动成功";
        } catch (ServiceOperationException e) {
            return "服务 " + SERVICENAME + " 启动失败: " + e.getMessage();
        }
    }

    /**
     * 停止服务
     */
    @PostMapping("/stop")
    public String stopService() {
        try {
            SmartServiceManager serviceManager = new SmartServiceManager(
                    host, port, username, password, timeout);
            serviceManager.stopService(SERVICENAME);
            return "服务 " + SERVICENAME + " 停止成功";
        } catch (ServiceOperationException e) {
            return "服务 " + SERVICENAME + " 停止失败: " + e.getMessage();
        }
    }

    /**
     * 重启服务
     */
    @PostMapping("/restart")
    public String restartService() {
        try {
            SmartServiceManager serviceManager = new SmartServiceManager(
                    host, port, username, password, timeout);
            serviceManager.restartService(SERVICENAME);
            return "服务 " + SERVICENAME + " 重启成功";
        } catch (ServiceOperationException e) {
            return "服务 " + SERVICENAME + " 重启失败: " + e.getMessage();
        }
    }

    /**
     * 获取服务状态
     */
    @GetMapping("/status")
    public String getStatus() {
        try {
            SmartServiceManager serviceManager = new SmartServiceManager(
                    host, port, username, password, timeout);
            ServiceInfo status = serviceManager.getStatus(SERVICENAME);
            return "服务 " + SERVICENAME + " 状态: " + status;
        } catch (ServiceOperationException e) {
            return "获取服务 " + SERVICENAME + " 状态失败: " + e.getMessage();
        }
    }

}
