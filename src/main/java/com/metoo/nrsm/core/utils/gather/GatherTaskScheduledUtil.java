package com.metoo.nrsm.core.utils.gather;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.api.ApiExecUtils;
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.snmp.utils.DeviceManager;
import com.metoo.nrsm.core.utils.license.AesEncryptUtils;
import com.metoo.nrsm.core.vo.LicenseVo;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FluxDailyRate;
import com.metoo.nrsm.entity.GradeWeight;
import com.metoo.nrsm.entity.License;
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
    @Autowired
    private ILicenseService licenseService;
    @Autowired
    private AesEncryptUtils aesEncryptUtils;
    @Autowired
    private ApiService apiService;
    @Autowired
    private DeviceManager deviceManager;

    private final ReentrantLock lock = new ReentrantLock();

    @Scheduled(cron = "0 */5 * * * ?")
//    @Scheduled(cron = "0 */1 * * * ?")
    public void api() {
        Map generalLog = new LinkedHashMap();
        generalLog.put("第一步：", "开始采集");
        if(traffic) {
            if (lock.tryLock()) {
                generalLog.put("第二步：", "获取锁");
                try {
                    Long time = System.currentTimeMillis();
                    log.info("Unit traffic start=================================");
                    try {

                        generalLog.put("第三步：", "流量推送开始");
                        apiExecUtils.exec();
                        generalLog.put("第四步：", "流量推送结束");
                    } catch (Exception e) {
                        log.error("Error unit traffic =================================" + e.getMessage());
                    }
                    generalLog.put("第五步：", "采集结束");
                    log.info("Unit traffic end=================================" + (System.currentTimeMillis()-time));
                } finally {
                    lock.unlock();
                    generalLog.put("第六步：", "释放锁");
                    try {
                        // 推送远程日志
                        String data = JSONObject.toJSONString(generalLog);
                        apiService.general(data);
                    } catch (Exception e) {
                        log.info("Unit traffic error =================================" + e.getMessage());
                        generalLog.put("第七步：", e.getMessage());
                    }
                }
            }
        }
    }

//    @Scheduled(fixedDelay = 300000)
//    @Scheduled(cron = "0 */1 * * * ?")
//    public void api() {
//        if(traffic) {
//            if (lock.tryLock()) {
//                try {
//                    Long time = System.currentTimeMillis();
//                    log.info("Unit traffic Start=================================");
//                    try {
//                        apiExecUtils.exec();
//                    } catch (Exception e) {
//                        log.error("Error unit traffic =================================" + e.getMessage());
//                    }
//                    log.info("Unit traffic End=================================" + (System.currentTimeMillis()-time));
//                } finally {
//                    lock.unlock();
//                }
//            }
//        }
//    }


    @Scheduled(cron = "0 */3 * * * ?")
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


    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
    public void mac() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("mac Start......");
            try {
                this.gatherService.gatherMac(DateTools.gatherDate(), new ArrayList<>());
//                gatherService.gatherMacThread(DateTools.gatherDate());
            } catch (Exception e) {
                log.error("Error occurred during MAC", e);
            }
            log.info("mac End......" + (System.currentTimeMillis()-time));
        }
    }

//    @Transactional // 可以结合该注解确调度任务在事务中运行，并在异常时正确回滚事务
//    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    @Scheduled(cron = "0 */3 * * * ?")
    public void ipv4() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("Ipv4 Start......");
            try {
//                gatherService.gatherIpv4(DateTools.gatherDate());
                gatherService.gatherIpv4Thread(DateTools.gatherDate(), new ArrayList<>());
            } catch (Exception e) {
                log.error("Error occurred during IPV4", e);
            }
            log.info("Ipv4 End......" + (System.currentTimeMillis()-time));
        }
    }

    @Scheduled(cron = "0 */3 * * * ?")
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

    @Scheduled(cron = "0 */3 * * * ?")
    public void ipv6() {
        if(flag){
            Long time=System.currentTimeMillis();
            log.info("Ipv6 Start......");
            try {
//                gatherService.gatherIpv6(DateTools.gatherDate());
                gatherService.gatherIpv6Thread(DateTools.gatherDate(), new ArrayList<>());
            } catch (Exception e) {
                log.error("Error occurred during IPV6", e);
            }
            log.info("Ipv6 End......" + (System.currentTimeMillis()-time));
        }
    }

    @Scheduled(cron = "0 */3 * * * ?")
    public void port() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("Port Start......");
            try {
                gatherService.gatherPort(DateTools.gatherDate(), new ArrayList<>());
            } catch (Exception e) {
                log.error("Error occurred during PORT", e);
            }
            log.info("Port End......" + (System.currentTimeMillis()-time));
        }
    }

    @Scheduled(cron = "0 */3 * * * ?")
    public void portIpv6() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("PortIpv6 Start......");
            try {
                gatherService.gatherPortIpv6(DateTools.gatherDate(), new ArrayList<>());
            } catch (Exception e) {
                log.error("Error occurred during PortIpv6", e);
            }
            log.info("PortIpv6 End......" + (System.currentTimeMillis()-time));
        }
    }


    @Scheduled(cron = "0 */3 * * * ?")
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


    // TODO 已同步|待增加并发采集
    @Scheduled(cron = "0 */1 * * * ?")
    public void snmpStatus() {
        if(flag){
            Long time = System.currentTimeMillis();
            log.info("Snmp status start......");
            try {
                 deviceManager.saveAvailableDevicesToRedis();
            } catch (Exception e) {
                log.error("Error occurred during SNMP", e);
            }
            log.info("Snmp status end......" + (System.currentTimeMillis()-time));
        }
    }



    @Scheduled(cron = "0 */3 * * * ?")
    public void probe() {
        if(false){
            Long time = System.currentTimeMillis();
            log.info("Probe start......");
            try {
                // 解析授权码，是否开启扫描
                if(getLicenseProbe()){
                    probeService.scanByTerminal();
                }
            } catch (Exception e) {
                log.error("Error occurred during Probe", e);
            }
            log.info("Probe end......" + (System.currentTimeMillis()-time));
        }
    }

    public boolean getLicenseProbe(){
        License obj = this.licenseService.query().get(0);
        String uuid = SystemInfoUtils.getSerialNumber();

        if (!uuid.equals(obj.getSystemSN())) {
           return false;
        }
        String licenseInfo = this.aesEncryptUtils.decrypt(obj.getLicense());
        LicenseVo licenseVo = JSONObject.parseObject(licenseInfo, LicenseVo.class);
        return licenseVo.isLicenseProbe();

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
