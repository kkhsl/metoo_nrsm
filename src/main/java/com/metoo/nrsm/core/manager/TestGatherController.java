package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.network.concurrent.PingThreadPool;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.PingCFScanner;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.*;
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
import org.springframework.scheduling.annotation.Scheduled;
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
@RequestMapping("/admin/test/gather")
@RestController
public class TestGatherController {

    @Autowired
    private IGatherService gatherService;
    @Autowired
    private GatherSingleThreadingMacSNMPUtils gatherSingleThreadingMacSNMPUtils;

    @GetMapping("/portIpv6")
    public void portIpv6() {
        log.info("Ipv6 Port采集任务开始");
        try {
            Long time = System.currentTimeMillis();
            gatherService.gatherPortIpv6(DateTools.gatherDate(), new ArrayList<>());
            log.info("Ipv6 Port采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Ipv6 Port采集任务异常: {}", e.getMessage());
        }
    }

    @GetMapping("/terminal")
    public void terminal() {
        this.gatherSingleThreadingMacSNMPUtils.updateTerminal(DateTools.gatherDate());
    }
}


