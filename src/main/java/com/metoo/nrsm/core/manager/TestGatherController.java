package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.mapper.MacTestMapper;
import com.metoo.nrsm.core.network.concurrent.PingThreadPool;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.PingCFScanner;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.service.impl.MacTestServiceImpl;
import com.metoo.nrsm.core.service.impl.ProbeServiceImpl;
import com.metoo.nrsm.core.system.conf.network.strategy.NetplanConfigManager;
import com.metoo.nrsm.core.system.conf.network.sync.LocalNetplanSyncService;
import com.metoo.nrsm.core.system.conf.network.sync.WindowsSshNetplanSyncService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMacUtils;
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
    private ProbeServiceImpl probeService;
    @Autowired
    private GatherSingleThreadingMacSNMPUtils gatherSingleThreadingMacSNMPUtils;

    @GetMapping("/snmp")
    public void snmp() {
        log.info("snmp采集任务开始");
        try {
            Long time = System.currentTimeMillis();
            gatherService.gatherSnmpStatus();
            log.info("snmp采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("snmp采集任务异常: {}", e.getMessage());
        }
    }

    @GetMapping("/port")
    public void port() {
        log.info("Port采集任务开始");
        try {
            Long time = System.currentTimeMillis();
            gatherService.gatherPort(DateTools.gatherDate(), new ArrayList<>());
            log.info("Port采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Port采集任务异常: {}", e.getMessage());
        }
    }

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

    @GetMapping("/writeTerminal")
    public void writeTerminal() {
        this.probeService.writeTerminal();
    }


    @GetMapping("/scanByTerminal")
    public void scanByTerminal() {
        this.probeService.scanByTerminal();
    }


    @Autowired
    private GatherMacUtils gatherMacUtils;

    @Autowired
    private MacTestServiceImpl macTestService;

//    @GetMapping("/updateMacTag")
//    private void updateMacTag(String step) {
//        switch (step) {
//            case "setTagToX":
//                gatherMacUtils.setTagToX(); // 读取mac表，与up接口的mac不重复标记为X，0:0:5e:0标记为V
//                break;
//            case "setTagToU":
//                gatherMacUtils.setTagToU(); // 标记U(1个mac对应1个port(除去L之外)，此条目标记为U)
//                break;
//            case "setTagToS":
//                gatherMacUtils.setTagToS();
//                break;
//            case "setTagSToE":
//                gatherMacUtils.setTagSToE();
//                break;
//            case "setTagXToE":
//                gatherMacUtils.setTagXToE();
//                break;
//            case "setTagUToE":
//                gatherMacUtils.setTagUToE();
//                break;
//            case "executeFullProcess":
//                macTestService.executeFullProcess();
//                break;
//            case "setTagSToRT":
//                gatherMacUtils.setTagSToRT();
//                break;
//            case "setTagUToRT":
//                gatherMacUtils.setTagUToRT();
//                break;
//            case "setTagRTToDT":
//                gatherMacUtils.setTagRTToDT();
//                break;
//            case "copyArpIpToMacByDT":
//                gatherMacUtils.copyArpIpToMacByDT();
//                break;
//            case "setTagRTToVDT":
//                gatherMacUtils.setTagRTToVDT(new Date());  // NSwitch
//                break;
//            case "setTagDTToVDE":
//                gatherMacUtils.setTagDTToVDE(); // （无线路由器）
//                break;
//            case "setTagRTToVDE":
//                gatherMacUtils.setTagRTToVDE();
//                break;
//            case "setTagRTToDTByDE":
//                gatherMacUtils.setTagRTToDTByDE();
//                break;
//            case "normalizePortForDE":
//                // DE条目remotePort修改为remoteIp对应的deviceIp的port，再在DE里面根据deviceIp和remoteIp去重
//                gatherMacUtils.normalizePortForDE();
//                break;
//            case "removeApTerminal":
//                gatherMacUtils.removeApTerminal(); // 删除mac与为ap mac地址相同的数据
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown step: " + step);
//        }
//    }


    @GetMapping("/updateMacTag")
    private String updateMacTag(String step) {
        String result = "ok";
        switch (step) {
            case "setTagXToE":
                gatherMacUtils.setTagToX();
                gatherMacUtils.setTagToU();
                gatherMacUtils.setTagToS();
                gatherMacUtils.setTagSToE();
                gatherMacUtils.setTagXToE();
                gatherMacUtils.setTagUToE();
                break;
            case "callRemoteIPAndPort":
                macTestMapper.callRemoteIPAndPort();
                break;
            case "executeFullProcess":
                result = executeFullProcess();
                break;
            case "setTagSToDE":
                gatherMacUtils.setTagSToRT();
                gatherMacUtils.setTagUToRT();
                gatherMacUtils.setTagRTToDT();
                gatherMacUtils.copyArpIpToMacByDT();
                gatherMacUtils.setTagRTToVDT(new Date());
                gatherMacUtils.setTagDTToVDE();
                gatherMacUtils.setTagRTToVDE();
                gatherMacUtils.setTagRTToDTByDE();
                gatherMacUtils.normalizePortForDE();
                gatherMacUtils.removeApTerminal(); // 删除mac与为ap mac地址相同的数据
                break;
            case "portToDE":
                gatherMacUtils.selectSameSubnetWithTwoPortsNotBothVlan(new Date());
                break;
            default:
                throw new IllegalArgumentException("Unknown step: " + step);
        }
        return result;
    }



    @Autowired
    private MacTestMapper macTestMapper;

    public String executeFullProcess() {
        boolean moreThanTwoExists = true;
        int count = macTestMapper.countMultiRecordDevices();
        if (count > 2) {
            macTestMapper.updateToAEForMultiples();
            macTestMapper.updateToDEForMultiples();
            macTestMapper.swapAEtoDEForMultiples();
            macTestMapper.updateToXEForMultiples();
        } else if (count == 2) {
            macTestMapper.updatePairedDEForTwo();
            moreThanTwoExists = false;
        } else {
            moreThanTwoExists = false;
        }
        if(macTestMapper.countMultiRecordDevices() > 2){
            return Boolean.toString(moreThanTwoExists);
        }
       return Boolean.toString(false);
    }
}


