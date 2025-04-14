package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMacUtils;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherSingleThreadingMacUtils;
import com.metoo.nrsm.core.utils.gather.thread.*;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/admin/gather/test/a")
@RestController
public class GatherTest {

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
    private PythonExecUtils pythonExecUtils;


    private final GatherDataThreadPool gatherDataThreadPool;
    @Autowired
    public GatherTest(GatherDataThreadPool gatherDataThreadPool) {
        this.gatherDataThreadPool = gatherDataThreadPool;
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
        for (NetworkElement networkElement : networkElements) {
            SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
            try {
                JSONArray result = SNMPv2Request.getArp(snmpParams);
                log.info("snmp result {}", result);
            } catch (Exception e) {
                System.out.println(e.getMessage());
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

    @GetMapping("ipv4")
    public void gatherIpv4() {
        List<NetworkElement> networkElements = this.getGatherDevice();
        if(networkElements.size() > 0) {
            for (NetworkElement networkElement : networkElements) {
                String path = Global.PYPATH + "getarp.py";

                String[] params = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String result = pythonExecUtils.exec(path, params);
                log.info("python result {}", result);
            }
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
