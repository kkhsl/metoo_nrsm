package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.mapper.MacTestMapper;
import com.metoo.nrsm.core.service.IGatherService;
import com.metoo.nrsm.core.service.impl.ProbeServiceImpl;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMacUtils;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherSingleThreadingMacSNMPUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;

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
    @Autowired
    private GatherMacUtils gatherMacUtils;
    @Autowired
    private MacTestMapper macTestMapper;


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

    @GetMapping("/mac")
    public void mac() {
        this.gatherService.gatherMac(DateTools.gatherDate(), new ArrayList<>());
    }

    @GetMapping("/writeTerminal")
    public void writeTerminal() {
        this.probeService.writeTerminal();
    }


    @GetMapping("/scanByTerminal")
    public void scanByTerminal() {
        this.probeService.scanByTerminal();
    }

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
        if (macTestMapper.countMultiRecordDevices() > 2) {
            return Boolean.toString(moreThanTwoExists);
        }
        return Boolean.toString(false);
    }
}


