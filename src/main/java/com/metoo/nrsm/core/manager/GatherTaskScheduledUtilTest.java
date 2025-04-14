package com.metoo.nrsm.core.manager;

import cn.hutool.core.date.DateTime;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMacUtils;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherSingleThreadingMacSNMPUtils;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherSingleThreadingMacUtils;
import com.metoo.nrsm.core.utils.gather.snmp.utils.MacManager;
import com.metoo.nrsm.core.utils.gather.thread.*;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.vo.SystemUsageVO;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/admin/gather/test")
@RestController
public class GatherTaskScheduledUtilTest {

    private boolean flag = true;
    @Autowired
    private IDhcpService dhcpService;
    @Autowired
    private IDhcp6Service dhcp6Service;
    @Autowired
    private IGatherService gatherService;
    @Autowired
    private IPingService pingService;
    @Autowired
    private ISubnetService subnetService;
    @Autowired
    private IFluxDailyRateService fluxDailyRateService;
    @Autowired
    private IGradWeightService gradWeightService;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private ISystemUsageService systemUsageService;
    @Autowired
    private SnmpStatusUtils snmpStatusUtils;
    @Autowired
    private Ipv4Service ipv4Service;
    @Autowired
    private Ipv6Service ipv6Service;
    @Autowired
    private IPortService portService;
    @Autowired
    private IPortIpv6Service portIpv6Service;
    @Autowired
    private IMacService macService;
    @Autowired
    private GatherMacUtils gatherMacUtils;
    @Autowired
    private GatherSingleThreadingMacUtils gatherSingleThreadingMacUtils;
    @Autowired
    private GatherSingleThreadingMacSNMPUtils gatherSingleThreadingMacSNMPUtils;

    @Autowired
    private MacManager macManager;
    @Autowired
    private ITerminalService terminalService;


    private final GatherDataThreadPool gatherDataThreadPool;
    @Autowired
    public GatherTaskScheduledUtilTest( GatherDataThreadPool gatherDataThreadPool) {
        this.gatherDataThreadPool = gatherDataThreadPool;
    }

    @GetMapping("terminal/unit2")
    public void terminalUnit2(){
        this.terminalService.writeTerminalUnitByUnit2();
    }

    @GetMapping("terminal/unit3")
    public void terminalUnit3(){
        Terminal terminal = this.terminalService.selectObjById(1039L);
        terminal.setAddTime(new DateTime());
        this.terminalService.update(terminal);
    }


    @GetMapping("selfMac1")
    public String selfMac(){

        Date date = DateTools.gatherDate();

        List<NetworkElement> networkElements = this.getGatherDevice();

        macService.truncateTableGather();

        CountDownLatch latch = new CountDownLatch(networkElements.size());

        for (NetworkElement networkElement : networkElements) {

            if(StringUtils.isBlank(networkElement.getVersion())
                    || StringUtils.isBlank(networkElement.getCommunity())){
                latch.countDown();
                continue;
            }

            GatherDataThreadPool.getInstance().addThread(new GatherMacSNMPRunnable(networkElement, new MacManager(), date, latch));
        }

        try {

            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "end";
    }

    @GetMapping("selfMac11")
    public String selfMac1(){

        Date date = DateTools.gatherDate();

        List<NetworkElement> networkElements = this.getGatherDevice();

        macService.truncateTableGather();

        CountDownLatch latch = new CountDownLatch(networkElements.size());

        for (NetworkElement networkElement : networkElements) {

            if(StringUtils.isBlank(networkElement.getVersion())
                    || StringUtils.isBlank(networkElement.getCommunity())){
                latch.countDown();
                continue;
            }

            GatherDataThreadPool.getInstance().addThread(new GatherMacSNMPRunnable2(networkElement, new MacManager(), date, latch));
        }

        try {

            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "end";
    }


    @GetMapping("selfMac2")
    public String selfMac2(){
        Date date = DateTools.gatherDate();

        List<NetworkElement> networkElements = this.getGatherDevice();

        macService.truncateTableGather();

        CountDownLatch latch = new CountDownLatch(networkElements.size());

        for (NetworkElement networkElement : networkElements) {

            String hostName = gatherSingleThreadingMacSNMPUtils.getHostName(networkElement);

            if (StringUtils.isNotEmpty(hostName)) {

                gatherSingleThreadingMacUtils.processNetworkElementData(networkElement, hostName, date);

            }

        }

        return "end";
    }

    @GetMapping("selfMac3")
    public String selfMac3(){
        Date date = DateTools.gatherDate();

        List<NetworkElement> networkElements = this.getGatherDevice();

        macService.truncateTableGather();

        CountDownLatch latch = new CountDownLatch(networkElements.size());

        for (NetworkElement networkElement : networkElements) {

            String hostName = gatherSingleThreadingMacSNMPUtils.getHostNameSNMP(networkElement);

            if (StringUtils.isNotEmpty(hostName)) {

                gatherSingleThreadingMacSNMPUtils.processNetworkElementData(networkElement, hostName, date);

            }

        }

        return "end";
    }


    @GetMapping("lldp")
    public void getMacLLDP() {

        Date date = new Date();

        List<NetworkElement> networkElements = this.getGatherDevice();
        for (NetworkElement networkElement : networkElements) {

            String hostName = SNMPv2Request.getDeviceName(new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity()));

            macManager.getLldpDataSNMP(networkElement, date, hostName);
        }

    }

    @GetMapping("getMac")
    public void getMac() {

        Date date = new Date();

        List<NetworkElement> networkElements = this.getGatherDevice();
        for (NetworkElement networkElement : networkElements) {

            String hostName = SNMPv2Request.getDeviceName(new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity()));

            macManager.getMacData(networkElement, date, hostName);
        }

    }

    @GetMapping("mac2")
    public void gatherMac2() {

        Date date = new Date();

        List<NetworkElement> networkElements = this.getGatherDevice();

        CountDownLatch latch = new CountDownLatch(networkElements.size());

//        gatherMacUtils.copyGatherData(date);

        macService.truncateTableGather();

        for (NetworkElement networkElement : networkElements) {

            if(StringUtils.isBlank(networkElement.getVersion())
                    || StringUtils.isBlank(networkElement.getCommunity())){
                latch.countDown();
                continue;
            }

            GatherDataThreadPool.getInstance().addThread(new GatherMacSNMPRunnable(networkElement, date, latch));

        }

        try {

            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @GetMapping("mac3")
    public void gatherMac3() {
        Date date = new Date();

        List<NetworkElement> networkElements = this.getGatherDevice();
        gatherSingleThreadingMacUtils.gatherMac(networkElements, date);
    }

    // 获取需要采集的设备
    public List<NetworkElement> getGatherDevice(){
        List<NetworkElement> networkElements = new ArrayList<>();
        Set<String> uuids = this.snmpStatusUtils.getOnlineDevice();
        if(uuids.size() > 0){
            for (String uuid : uuids) {
                NetworkElement networkElement = this.networkElementService.selectObjByUuid(uuid);
                if(networkElement != null
                        && StringUtils.isNotBlank(networkElement.getIp())
                        && StringUtils.isNotBlank(networkElement.getVersion())
                        && StringUtils.isNotBlank(networkElement.getCommunity())){
                    networkElements.add(networkElement);
                }
            }
        }
        return networkElements;
    }

    @GetMapping("/snmp/ipv4")
    public void gatherIpv4Thread() {

        Date date = new Date();

        List<NetworkElement> networkElements = this.getGatherDevice();

        if(networkElements.size() > 0) {

            this.ipv4Service.truncateTableGather();

            CountDownLatch latch = new CountDownLatch(networkElements.size());

            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion())
                        || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    continue;
                }

                gatherDataThreadPool.addThread(new GatherIPv4SNMPRunnable(networkElement, date, latch));
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/snmp/ipv6")
    public void gatherIpv6Thread() {
        Date date = new Date();

        List<NetworkElement> networkElements = this.getGatherDevice();

        if(networkElements.size() > 0) {


            this.ipv6Service.truncateTableGather();

            CountDownLatch latch = new CountDownLatch(networkElements.size());
            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    continue;
                }

                GatherDataThreadPool.getInstance().addThread(new GatherIpV6SNMPRunnable(networkElement, date, latch));

            }
            try {

                latch.await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @GetMapping("/snmp/port")
    public void port() {
        Date date = new Date();
        List<NetworkElement> networkElements = this.getGatherDevice();

        if(networkElements.size() > 0) {

            this.portService.truncateTableGather();

            CountDownLatch latch = new CountDownLatch(networkElements.size());

            for (NetworkElement networkElement : networkElements) {


                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    continue;
                }

                GatherDataThreadPool.getInstance().addThread(new GatherPortSNMPRunnable(networkElement, date, latch));
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/snmp/port/v6")
    public void portV6() {
        Date date = new Date();
        List<NetworkElement> networkElements = this.getGatherDevice();
        if(networkElements.size() > 0) {

            this.portIpv6Service.truncateTableGather();

            CountDownLatch latch = new CountDownLatch(networkElements.size());

            for (NetworkElement networkElement : networkElements) {
                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    continue;
                }
                GatherDataThreadPool.getInstance().addThread(new GatherPortIpv6SNMPRunnable(networkElement, date, latch));
            }

            try {
                latch.await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @GetMapping("/snmp/port/isv6")
    public void portISV6() {

        Date date = new Date();
        List<NetworkElement> networkElements = this.getGatherDevice();

        CountDownLatch latch = new CountDownLatch(networkElements.size());

        if(networkElements.size() > 0) {

            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    continue;
                }

                GatherDataThreadPool.getInstance().addThread(new GatherIsIpv6SNMPRunnable(networkElement, date, latch));
            }

            try {

                latch.await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


    @PostMapping("start")
    public Result gather(@RequestBody(required = false) List<String> ips){
        Date start = new Date();
        Map params = new HashMap();
        params.put("ips", ips);
        List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
        List list = new ArrayList();
        String msg = "";

        log.info("dhcp ===== ");
        dhcpService.gather(DateTools.gatherDate());

        log.info("dhcp6 ===== ");
        dhcp6Service.gather(DateTools.gatherDate());

        log.info("IPV4 ===== ");
        Map ipv4LogMessages = gatherService.gatherIpv4Thread(DateTools.gatherDate(), networkElements);
        list.add(ipv4LogMessages);

        msg = "IPv4 ARP采集完成";
        list.add(msg);
        log.info("IPV4Detail ===== ");
        gatherService.gatherIpv4Detail(DateTools.gatherDate());

        log.info("IPV6 ===== ");
        Map ipv6LogMessages = gatherService.gatherIpv6Thread(DateTools.gatherDate(), networkElements);
        list.add(ipv6LogMessages);

        log.info("PORT ===== ");
        gatherService.gatherPort(DateTools.gatherDate(), networkElements);
        log.info("PORT6 ===== ");
        gatherService.gatherPortIpv6(DateTools.gatherDate(), networkElements);
        msg = "IPv6 ARP采集完成";
        list.add(msg);
//        gatherService.gatherIsIpv6(DateTools.gatherDate());

        log.info("ARP ===== ");
        gatherService.gatherArp(DateTools.gatherDate());
        msg = "arp分析完成";
        list.add(msg);

        log.info("MAC ===== ");
        Map macLogMessages = gatherService.gatherMac(DateTools.gatherDate(), networkElements);
        list.add(macLogMessages);

        msg = "mac采集完成";
        list.add(msg);

//        probeService.scanByTerminal();

        Date end = new Date();

        params.clear();
        params.put("startTime", start);
        params.put("endTime", end);
        List<SystemUsageVO> systemUsageList = this.systemUsageService.selectObjVOByMap(params);
        list.addAll(systemUsageList);

        return ResponseUtil.ok(list);

    }

    // 第一步：检查设备snmp状态
    @GetMapping("snmp/status")
    public void snmpStatus() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("snmp status Start......");
            try {
                this.gatherService.gatherSnmpStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("snmp status End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("dhcp")
    public void gatherDhcp() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("DHCP Start......");
            try {
                dhcpService.gather(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("DHCP End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("dhcp6")
    public void gatherDhcp6() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("DHCP6 Start......");
            try {
                dhcp6Service.gather(DateTools.gatherDate());

            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("DHCP6 End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("arp")
    public void gatherArp() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("arp Start......");
            try {
//                arpService.gatherArp(date);
                gatherService.gatherArp(DateTools.gatherDate());
            } catch (Exception e) {

                e.printStackTrace();
            }
            log.info("arp End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("mac")
    public void gatherMac() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("mac Start......");
            try {
                gatherService.gatherMac(DateTools.gatherDate(), new ArrayList<>());
//                gatherService.gatherMacThread(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("mac End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("ipv4")
    public void gatherIpv4() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("Ipv4 Start......");
            try {
                gatherService.gatherIpv4(DateTools.gatherDate());
//                gatherService.gatherIpv4Thread(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("Ipv4 End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("ipv4/detail")
    public void gatherIpv4Detail() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("Ipv4 Start......");
            try {
                gatherService.gatherIpv4Detail(DateTools.gatherDate());
//                gatherService.gatherIpv4Thread(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("Ipv4 End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("ipv6")
    public void gatherIpv6() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("Ipv6 Start......");
            try {
                gatherService.gatherIpv6(DateTools.gatherDate(), new ArrayList<>());
//                gatherService.gatherIpv6Thread(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("Ipv6 End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("port")
    public void gatherPort() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("Port Start......");
            try {
                gatherService.gatherPort(DateTools.gatherDate(), new ArrayList<>());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("Port End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("port/ipv6")
    public void gatherPortIpv6() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("PortIpv6 Start......");
            try {
                gatherService.gatherPortIpv6(DateTools.gatherDate(), new ArrayList<>());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("PortIpv6 End......" + (System.currentTimeMillis()-time));
        }
    }

    ////////////////////////////////////////////

    @GetMapping("isipv6")
    public void isIpv6() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("IsIpv6 Start......");
            try {
                gatherService.gatherIsIpv6(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("IsIpv6 End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("flux")
    public void flux() {
        if(flag) {
            Long time = System.currentTimeMillis();
            log.info("flux Start......");
            try {
                gatherService.gatherFlux(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("flux End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("ping")
    public void ping() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("ping Start......");
            try {
                pingService.exec();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("ping End......" + (System.currentTimeMillis()-time));
        }
    }

    @GetMapping("ping/subnet")
    public void pingSubnet() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("ping subnet Start......");
            try {
                this.subnetService.pingSubnet();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("ping subnet End......" + (System.currentTimeMillis()-time));
        }
    }



    @GetMapping("flux/daily/rate")
    public void fluxDailyRate() {
        Date endOfDay = DateTools.getEndOfDay();
        FluxDailyRate fluxDailyRate = new FluxDailyRate();
        fluxDailyRate.setRate(new BigDecimal(0));
        fluxDailyRate.setAddTime(endOfDay);
        Map params = new HashMap();
        params.clear();
        params.put("startOfDay", DateTools.getStartOfDay());
        params.put("endOfDay", endOfDay);
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
        if(flowStatisticsList.size() > 0){
            BigDecimal sum = flowStatisticsList.stream().filter(e -> e.getIpv6Rate() != null).map(FlowStatistics::getIpv6Rate)
                    .collect(Collectors.toList())
                    .stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            long count = flowStatisticsList.stream().filter(e -> e.getIpv6Rate() != null).map(FlowStatistics::getIpv6Rate)
                    .collect(Collectors.toList())
                    .stream().count();

            if(sum.compareTo(new BigDecimal(0)) >= 1){
                BigDecimal rate = sum.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                fluxDailyRate.setRate(rate);
                GradeWeight gradeWeight = this.gradWeightService.selectObjOne();
                if(gradeWeight != null){
                    if(gradeWeight.getReach() != null && gradeWeight.getReach().compareTo(new BigDecimal(0)) >= 1){
                        if(rate.compareTo(gradeWeight.getReach()) > -1){
                            fluxDailyRate.setFlag(true);
                        }
                    }
                }
            }
        }
        this.fluxDailyRateService.save(fluxDailyRate);
    }

}
