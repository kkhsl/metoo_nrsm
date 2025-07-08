package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.network.concurrent.PingThreadPool;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.PingCFScanner;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.system.conf.network.strategy.NetplanConfigManager;
import com.metoo.nrsm.core.system.conf.network.sync.LocalNetplanSyncService;
import com.metoo.nrsm.core.system.conf.network.sync.WindowsSshNetplanSyncService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherSingleThreadingMacSNMPUtils;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.core.utils.system.DiskInfo;
import com.metoo.nrsm.entity.Interface;
import com.metoo.nrsm.entity.Subnet;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import javax.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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


