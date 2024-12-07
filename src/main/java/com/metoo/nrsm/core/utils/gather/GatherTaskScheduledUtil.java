package com.metoo.nrsm.core.utils.gather;

import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.api.ApiExecUtils;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FluxDailyRate;
import com.metoo.nrsm.entity.GradeWeight;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-18 16:03
 */
@Slf4j
@Configuration
public class GatherTaskScheduledUtil {

    @Value("${task.switch.is-open}")
    private boolean flag;
    @Value("${task.switch.traffic.is-open}")
    private boolean traffic;
    @Autowired
    private IDhcpService dhcpService;
    @Autowired
    private IDhcp6Service dhcp6Service;
    @Autowired
    private IGatherService gatherService;
    @Autowired
    private IProbeService probeService;
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


    @Scheduled(cron = "0 */5 * * * ?")
    public void api() {
        if(traffic) {
            Long time = System.currentTimeMillis();
            log.info("unit traffic Start=================================");
            try {
                apiExecUtils.exec2();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("unit traffic End=================================" + (System.currentTimeMillis()-time));
        }
    }

    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
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

//    @org.springframework.scheduling.annotation.Scheduled(cron = "*/10 * * * * ?")
    @Scheduled(cron = "0 */3 * * * ?")
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

//    @Transactional // 可以结合该注解确调度任务在事务中运行，并在异常时正确回滚事务
    @Scheduled(cron = "0 */3 * * * ?")
//    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void gatherIpv4() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("Ipv4 Start......");
            try {
//                gatherService.gatherIpv4(DateTools.gatherDate());
                gatherService.gatherIpv4Thread(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("Ipv4 End......" + (System.currentTimeMillis()-time));
        }
    }

    @Scheduled(cron = "0 */3 * * * ?")
//    @Scheduled(fixedRate = 60000) // 每60秒执行一次
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

    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
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

    ////////////////

    @Scheduled(cron = "0 */2 * * * ?")
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

    @Scheduled(cron = "0 */30 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
    public void gatherProbe() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("Probe start......");
            try {
                probeService.scanByTerminal();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("Probe end......" + (System.currentTimeMillis()-time));
        }
    }


    @Scheduled(cron = "59 59 23 * * ?")
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
