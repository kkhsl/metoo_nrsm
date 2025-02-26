package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPRequest;
import com.metoo.nrsm.core.network.snmp4j.concurrent.SNMPThreadPoolManager;
import com.metoo.nrsm.core.network.snmp4j.mockito.SNMPCommandHandler;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.entity.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/admin/test")
public class TestSNMPController {

    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private SNMPCommandHandler snmpCommandHandler;

    @GetMapping("/snmpCommandHandler")
    public void snmpCommandHandler() throws Exception {
        // 模拟配置文件中的方法
        String vendor = "h3c";
        String command = "test";
        // 调用需要测试的方法
        snmpCommandHandler.handleCommand(vendor, command, "192.168.1.1", "2c", "public", "1.3.6.1.2.1.1");
    }


    @GetMapping("/getHostName")
    public void getHostName(){

        List<Callable<String>> tasks = new ArrayList<>();

        List<NetworkElement> networkElements = networkElementService.selectObjAllByGather();


        // 遍历所有 NetWorkElement，创建任务
        for (NetworkElement element : networkElements) {
            tasks.add(() -> {
                SNMPParams snmpParams = new SNMPParams(element.getIp(), element.getVersion(), element.getCommunity());
                String deviceName = SNMPRequest.getDeviceName(snmpParams);  // 获取设备名
                return deviceName;
            });
        }

        // 提交任务并返回结果
        List<String> deviceInfos = new ArrayList<>();
        try {
            List<Future<String>> results = SNMPThreadPoolManager.submitTasks(tasks);
            for (Future<String> result : results) {
                try {
                    String deviceInfo = result.get();  // 获取每个任务的结果
                    deviceInfos.add(deviceInfo);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();  // 处理任务异常
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(deviceInfos);
    }
}
