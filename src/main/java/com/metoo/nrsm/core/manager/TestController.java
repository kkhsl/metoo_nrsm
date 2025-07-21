package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.thirdparty.api.traffic.TimeUtils;
import com.metoo.nrsm.core.thirdparty.api.traffic.TrafficPullApi;
import com.metoo.nrsm.core.network.concurrent.PingThreadPool;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.PingCFScanner;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.system.conf.network.strategy.NetplanConfigManager;
import com.metoo.nrsm.core.system.conf.network.sync.LocalNetplanSyncService;
import com.metoo.nrsm.core.system.conf.network.sync.WindowsSshNetplanSyncService;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequestMapping("/admin/test")
@RestController
public class TestController {

    @Autowired
    private IDnsLogService dnsLogService;
    @Resource
    private IDnsRecordService recordService;
    @Autowired
    private ISubnetService subnetService;
    @Autowired
    private WindowsSshNetplanSyncService remoteSyncService;

    /**
     * 注意删除该引用jar，使用了另一个
     */
    @Test
    public void getArpV6() {
        System.out.println(JSONObject.class.getProtectionDomain().getCodeSource().getLocation());
    }

    @Autowired
    private TrafficPullApi trafficPullApi;
    @Autowired
    private IFlowUnitService flowUnitService;
    @Autowired
    private ITrafficService trafficService;
    @Autowired
    private IProbeService probeService;

    @GetMapping("/waits") // 'wait()' cannot override 'wait()' in 'java.lang.Object'; overridden method is final
    public void waits() throws InterruptedException {
        this.probeService.wart();
    }


    @GetMapping("/insert")
    public void insert() throws InterruptedException {
        this.probeService.insertProbe();
    }


    @GetMapping("/traffic/api")
    public void trafficAPI(){
        LocalDateTime baseTime = TimeUtils.getNow();
        String currentTime = TimeUtils.format(TimeUtils.clearSecondAndNano(baseTime));
        String fiveMinutesBefore = TimeUtils.format(TimeUtils.getFiveMinutesBefore(baseTime));
        List<FlowUnit> flowUnits = flowUnitService.selectObjByMap(Collections.emptyMap());
        if (flowUnits == null || flowUnits.isEmpty()) {
            log.info("未获取到任何单位，跳过调用 NetFlow API。");
            return;
        }
        for (FlowUnit flowUnit : flowUnits) {

            String unitName = flowUnit.getUnitName();
            if (unitName == null || unitName.trim().isEmpty()) {
                log.warn("单位名称为空，跳过该单位的流量查询。");
                continue;
            }

            TrafficPullApi.ApiResponse response = trafficPullApi.queryNetFlow(unitName, fiveMinutesBefore, currentTime);

            if (!response.isSuccess()) {
                log.error("调用 NetFlow API 失败，单位: {}, 错误信息: {}", unitName, response.getError());
                continue;
            }
            Map<String, Object> dataMap = Optional.ofNullable(response.getData())
                    .map(d -> (Map<String, Object>) d.get("data"))
                    .orElse(null);

            if (dataMap == null) {
                log.warn("NetFlow API 返回空数据，单位: {}", unitName);
                continue;
            }
            Traffic traffic = new Traffic();
            traffic.setUnitName(String.valueOf(dataMap.getOrDefault("name", unitName)));
            traffic.setVfourFlow(String.valueOf(dataMap.getOrDefault("ipv4Flow", "0.0")));
            traffic.setVsixFlow(String.valueOf(dataMap.getOrDefault("ipv6Flow", "0.0")));
            traffic.setAddTime(Date.from(baseTime.atZone(ZoneId.systemDefault()).toInstant()));
            trafficService.save(traffic);
            log.info("成功保存流量数据: 单位: {}, IPv4: {}, IPv6: {}", traffic.getUnitName(),
                    traffic.getVfourFlow(), traffic.getVsixFlow());
        }
    }

    // interface vlans
    public void interfaceVlans() {
        Interface vlan200 = new Interface();
        vlan200.setName("eno1");
        vlan200.setVlanNum(200);
        vlan200.setIpv4Address("192.168.6.102/24");
        vlan200.setIpv6Address("fc00:1000:0:1::3/64");
        vlan200.setGateway4("192.168.6.1");

        try {
            NetplanConfigManager.updateInterfaceConfig(vlan200);
            System.out.println("VLAN 200配置更新成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @GetMapping("/sync/network/remote")
    public void sync() {
        remoteSyncService.syncInterfaces();
    }

    @Autowired
    private LocalNetplanSyncService localSyncService;

    @GetMapping("/sync/network/local")
    public void localSync() {
        localSyncService.syncInterfaces();
    }

    @GetMapping("cf-scanner")
    public void cfscanner() {
        try {
            List<Subnet> subnets = this.subnetService.leafIpSubnetMapper(null);
            if (subnets.size() > 0) {
                for (Subnet subnet : subnets) {
                    if (MyStringUtils.isNonEmptyAndTrimmed(subnet.getIp())
                            && subnet.getMask() != null) {

                        if (subnet.getMask() == 32) {
                            PingCFScanner.scan(subnet.getIp());
                        } else {
                            PingCFScanner.scan(subnet.getIp() + "/" + subnet.getMask());
                        }
                    }
                }
            }
        } finally {
            // 关闭线程池
            // 等待全局线程池任务完成
            PingThreadPool.shutdown(); // 平滑关闭
            try {
                if (!PingThreadPool.awaitTermination(5, TimeUnit.MINUTES)) {
                    log.error("强制终止未完成任务");
                    PingThreadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                PingThreadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @GetMapping("/analysisDnsLogTask")
    public void analysisDnsLogTask() {
        log.info("====================================解析dns日志并保存汇总数据开始执行==========================");
        try {
            //删除之前的临时数据
            dnsLogService.truncateTable();
            // 解析日志文件并入库
            dnsLogService.parseLargeLog();
            // 获取解析的数据并汇总入库
            recordService.saveRecord();
        } catch (Exception e) {
            log.error("定时任务解析dns日志并保存汇总数据出现错误：{}", e);
        }
        log.info("====================================解析dns日志并保存汇总数据定时任务结束==========================");
    }

}


