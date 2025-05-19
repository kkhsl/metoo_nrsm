package com.metoo.nrsm.core.manager.service;

import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.manager.SmartServiceManager;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service")
public class RadvdServiceController {

    private final String host = "192.168.6.102"; // 或者配置文件中获取
    private final int port = 22;
    private final String username = "root";
    private final String password = "Metoo89745000!";
    private final int timeout = 5000; // 超时 5 秒

    private final String serviceName = "radvd";

    /**
     * 启动服务
     */
    @PostMapping("/start")
    public String startService() {
        try {
            SmartServiceManager serviceManager = new SmartServiceManager();
            serviceManager.startService(serviceName);
            return "服务 " + serviceName + " 启动成功";
        } catch (ServiceOperationException e) {
            return "服务 " + serviceName + " 启动失败: " + e.getMessage();
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
            serviceManager.stopService(serviceName);
            return "服务 " + serviceName + " 停止成功";
        } catch (ServiceOperationException e) {
            return "服务 " + serviceName + " 停止失败: " + e.getMessage();
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
            serviceManager.restartService(serviceName);
            return "服务 " + serviceName + " 重启成功";
        } catch (ServiceOperationException e) {
            return "服务 " + serviceName + " 重启失败: " + e.getMessage();
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
            ServiceInfo status = serviceManager.getStatus(serviceName);
            return "服务 " + serviceName + " 状态: " + status;
        } catch (ServiceOperationException e) {
            return "获取服务 " + serviceName + " 状态失败: " + e.getMessage();
        }
    }

}
