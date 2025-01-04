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

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
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
    private ISubnetService subnetService;
    @Autowired
    private IFluxDailyRateService fluxDailyRateService;
    @Autowired
    private IGradWeightService gradWeightService;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;
    @Autowired
    private ApiExecUtils apiExecUtils;

    private final ReentrantLock lock = new ReentrantLock();

//    @Scheduled(fixedDelay = 300000)
    @Scheduled(cron = "0 */5 * * * ?")
    public void api() {
        if(traffic) {
            if (lock.tryLock()) {
                try {
                    Long time = System.currentTimeMillis();
                    log.info("Unit traffic Start=================================");
                    try {
                        apiExecUtils.exec2();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    log.info("Unit traffic End=================================" + (System.currentTimeMillis()-time));
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    // @Scheduled 默认使用单线程来执行定时任务。如果某次任务执行时间过长（例如阻塞操作），后续的任务会被延迟执行，甚至可能导致任务积压，最终无法执行
//    @Scheduled(cron = "0 */5 * * * ?")
//    @Scheduled(fixedDelay = 300000)
//    public void api() {
//        if(traffic) {
//            Long time = System.currentTimeMillis();
//            log.info("unit traffic Start=================================");
//            try {
//                apiExecUtils.exec2();
//            } catch (Exception e) {
//                log.error("Error occurred during API", e);
//            }
//            log.info("unit traffic End=================================" + (System.currentTimeMillis()-time));
//        }
//    }

//    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void dhcp() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("DHCP Start......");
            try {
                dhcpService.gather(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during DHCP", e);
            }
            log.info("DHCP End......" + (System.currentTimeMillis()-time));
        }
    }

    //    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void dhcp6() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("DHCP6 Start......");
            try {
                dhcp6Service.gather(DateTools.gatherDate());

            } catch (Exception e) {
                log.error("Error occurred during DHCP6", e);
            }
            log.info("DHCP6 End......" + (System.currentTimeMillis()-time));
        }
    }

    //    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void arp() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("arp Start......");
            try {
//                arpService.gatherArp(date);
                gatherService.gatherArp(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during ARP", e);
            }
            log.info("arp End......" + (System.currentTimeMillis()-time));
        }
    }

//    @org.springframework.scheduling.annotation.Scheduled(cron = "*/10 * * * * ?")
//    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void mac() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("mac Start......");
            try {
                this.gatherService.gatherMac(DateTools.gatherDate());
//                gatherService.gatherMacThread(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during MAC", e);
            }
            log.info("mac End......" + (System.currentTimeMillis()-time));
        }
    }

//    @Transactional // 可以结合该注解确调度任务在事务中运行，并在异常时正确回滚事务
//    @Scheduled(fixedRate = 60000) // 每60秒执行一次
//    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void ipv4() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("Ipv4 Start......");
            try {
//                gatherService.gatherIpv4(DateTools.gatherDate());
                gatherService.gatherIpv4Thread(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during IPV4", e);
            }
            log.info("Ipv4 End......" + (System.currentTimeMillis()-time));
        }
    }

//    @Scheduled(fixedRate = 60000) // 每60秒执行一次
//    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void ipv4Detail() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("Ipv4 Start......");
            try {
                gatherService.gatherIpv4Detail(DateTools.gatherDate());
//                gatherService.gatherIpv4Thread(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during IPV4Detail", e);
            }
            log.info("Ipv4 End......" + (System.currentTimeMillis()-time));
        }
    }

    //    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void ipv6() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("Ipv6 Start......");
            try {
                gatherService.gatherIpv6(DateTools.gatherDate());
//                gatherService.gatherIpv6Thread(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during IPV6", e);
            }
            log.info("Ipv6 End......" + (System.currentTimeMillis()-time));
        }
    }

    //    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void port() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("Port Start......");
            try {
                gatherService.gatherPort(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during PORT", e);
            }
            log.info("Port End......" + (System.currentTimeMillis()-time));
        }
    }

    //    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void portIpv6() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("PortIpv6 Start......");
            try {
                gatherService.gatherPortIpv6(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during PortIpv6", e);
            }
            log.info("PortIpv6 End......" + (System.currentTimeMillis()-time));
        }
    }

    ////////////////////////////////////////////

    //    @Scheduled(cron = "0 */3 * * * ?")
    @Scheduled(fixedDelay = 180000)
    public void isIpv6() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("IsIpv6 Start......");
            try {
                gatherService.gatherIsIpv6(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during isIpv6", e);
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
                log.error("Error occurred during Flux", e);
            }
            log.info("flux End......" + (System.currentTimeMillis()-time));
        }
    }

    ////////////////

//    @Scheduled(cron = "0 */2 * * * ?")
//    public void ping() {
//        if(flag){
//            Long time = System.currentTimeMillis();
//            log.info("ping Start......");
//            try {
//                pingService.exec();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            log.info("ping End......" + (System.currentTimeMillis()-time));
//        }
//    }

    @Scheduled(cron = "0 */30 * * * ?")
    public void pingSubnet() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("ping subnet Start......");
            try {
                this.subnetService.pingSubnet();
            } catch (Exception e) {
                log.error("Error occurred during Subnet", e);
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
                log.error("Error occurred during SNMP", e);
            }
            log.info("snmp status End......" + (System.currentTimeMillis()-time));
        }
    }

    @Scheduled(cron = "0 */3 * * * ?")
    public void probe() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("Probe start......");
            try {
                probeService.scanByTerminal();
            } catch (Exception e) {
                log.error("Error occurred during Probe", e);
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
