package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.api.ApiExecUtils;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FluxDailyRate;
import com.metoo.nrsm.entity.GradeWeight;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private ApiExecUtils apiExecUtils;

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
                gatherService.gatherMac(DateTools.gatherDate());
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
                gatherService.gatherIpv6(DateTools.gatherDate());
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
                gatherService.gatherPort(DateTools.gatherDate());
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
                gatherService.gatherPortIpv6(DateTools.gatherDate());
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
