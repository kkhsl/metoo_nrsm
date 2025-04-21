package com.metoo.nrsm.core.utils.gather;

import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.mapper.TrafficDataMapper;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.api.ApiExecUtils;
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.snmp.utils.DeviceManager;
import com.metoo.nrsm.core.utils.license.AesEncryptUtils;
import com.metoo.nrsm.core.vo.LicenseVo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    @Resource
    private TerminalUnitMapper terminalUnitMapper;

    @Resource
    private TrafficDataMapper trafficDataMapper;

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
                    log.info("FlowUnit traffic start=================================");
                    try {

                        generalLog.put("第三步：", "流量推送开始");
                        apiExecUtils.exec();
                        generalLog.put("第四步：", "流量推送结束");
                    } catch (Exception e) {
                        log.error("Error unit traffic =================================" + e.getMessage());
                    }
                    generalLog.put("第五步：", "采集结束");
                    log.info("FlowUnit traffic end=================================" + (System.currentTimeMillis()-time));
                } finally {
                    lock.unlock();
                    generalLog.put("第六步：", "释放锁");
                    try {
                        // 推送远程日志
                        String data = JSONObject.toJSONString(generalLog);
                        apiService.general(data);
                    } catch (Exception e) {
                        log.info("FlowUnit traffic error =================================" + e.getMessage());
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
//                    log.info("FlowUnit traffic Start=================================");
//                    try {
//                        apiExecUtils.exec();
//                    } catch (Exception e) {
//                        log.error("Error unit traffic =================================" + e.getMessage());
//                    }
//                    log.info("FlowUnit traffic End=================================" + (System.currentTimeMillis()-time));
//                } finally {
//                    lock.unlock();
//                }
//            }
//        }
//    }


    private volatile boolean isRunningDhcp = false;
    @Scheduled(fixedDelay = 180_000)
    public void dhcp() {
        if(flag && !isRunningDhcp){
            isRunningDhcp = true;
            try {
                Long time=System.currentTimeMillis();
                log.info("DHCP Start......");
                try {
                    dhcpService.gather(DateTools.gatherDate());
                } catch (Exception e) {
                    log.error("Error occurred during DHCP", e);
                }
                log.info("DHCP End......" + (System.currentTimeMillis()-time));
            } finally {
                isRunningDhcp = false;
            }
        }
    }

    private volatile boolean isRunningDhcp6 = false;
    @Scheduled(fixedDelay = 180_000)
    public void dhcp6() {
        if(flag && !isRunningDhcp6){
            isRunningDhcp6 = true;
            try {
                Long time=System.currentTimeMillis();
                log.info("DHCP6 Start......");
                try {
                    dhcp6Service.gather(DateTools.gatherDate());

                } catch (Exception e) {
                    log.error("Error occurred during DHCP6", e);
                }
                log.info("DHCP6 End......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningDhcp6 = false;
            }
        }
    }

    private volatile boolean isRunningARP = false;
    @Scheduled(fixedDelay = 180_000)
    public void arp() {
        if(flag && !isRunningARP){
            isRunningARP = true;
            try {
                Long time=System.currentTimeMillis();
                log.info("arp Start......");
                try {
                    //                arpService.gatherArp(date);
                    gatherService.gatherArp(DateTools.gatherDate());
                } catch (Exception e) {
                    log.error("Error occurred during ARP", e);
                }
                log.info("arp End......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningARP = false;
            }
        }
    }

    private volatile boolean isRunningMAC = false;
    @Scheduled(fixedDelay = 180_000)
    public void mac() {
        if(flag && !isRunningMAC){
            isRunningMAC = true;
            try {
                Long time=System.currentTimeMillis();
                log.info("mac Start......");
                try {
                    this.gatherService.gatherMac(DateTools.gatherDate(), new ArrayList<>());
                    //                gatherService.gatherMacThread(DateTools.gatherDate());
                } catch (Exception e) {
                    log.error("Error occurred during MAC", e);
                }
                log.info("mac End......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningMAC = false;
            }
        }
    }

//    @Transactional // 可以结合该注解确调度任务在事务中运行，并在异常时正确回滚事务
//    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    private volatile boolean isRunningIPV4 = false;
    @Scheduled(fixedDelay = 180_000)
    public void ipv4() {
        if(flag && !isRunningIPV4){
            isRunningIPV4 = true;
            try {
                Long time=System.currentTimeMillis();
                log.info("Ipv4 Start......");
                try {
    //                gatherService.gatherIpv4(DateTools.gatherDate());
                    gatherService.gatherIpv4Thread(DateTools.gatherDate(), new ArrayList<>());
                } catch (Exception e) {
                    log.error("Error occurred during IPV4", e);
                }
                log.info("Ipv4 End......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningIPV4 = false;
            }
        }
    }

    private volatile boolean isRunningIPV4Detail = false;
    @Scheduled(fixedDelay = 180_000)
    public void ipv4Detail() {
        if(flag && !isRunningIPV4Detail){
            isRunningIPV4Detail = true;
            try {
                Long time=System.currentTimeMillis();
                log.info("Ipv4 detail start......");
                try {
                    gatherService.gatherIpv4Detail(DateTools.gatherDate());
    //                gatherService.gatherIpv4Thread(DateTools.gatherDate());
                } catch (Exception e) {
                    log.error("Error occurred during IPV4Detail", e);
                }
                log.info("Ipv4 detail end......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningIPV4Detail = false;
            }
        }
    }

    private volatile boolean isRunningPort = false;
    @Scheduled(fixedDelay = 180_000)
    public void port() {
        if(flag && !isRunningPort){
            isRunningPort = true;
            Long time = System.currentTimeMillis();
            log.info("Port Start......");
            try {
                gatherService.gatherPort(DateTools.gatherDate(), new ArrayList<>());
            } catch (Exception e) {
                log.error("Error occurred during PORT", e);
            }
            try {
                log.info("Port End......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningPort = false;
            }
        }
    }



    private volatile boolean isRunningIPV6 = false;
    @Scheduled(fixedDelay = 180_000)
    public void ipv6() {
        if(flag && !isRunningIPV6){
            isRunningIPV6 = true;
            try {
                Long time=System.currentTimeMillis();
                log.info("Ipv6 Start......");
                try {
                    // gatherService.gatherIpv6(DateTools.gatherDate());
                    gatherService.gatherIpv6Thread(DateTools.gatherDate(), new ArrayList<>());
                } catch (Exception e) {
                    log.error("Error occurred during IPV6", e);
                }
                log.info("Ipv6 End......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningIPV6 = false;
            }
        }
    }

    private volatile boolean isRunningIPV6Port = false;
    @Scheduled(fixedDelay = 180_000)
    public void portIpv6() {
        if(flag && !isRunningIPV6Port){
            isRunningIPV6Port = true;
            Long time = System.currentTimeMillis();
            log.info("PortIpv6 Start......");
            try {
                gatherService.gatherPortIpv6(DateTools.gatherDate(), new ArrayList<>());
            } catch (Exception e) {
                log.error("Error occurred during PortIpv6", e);
            }
            try {
                log.info("PortIpv6 End......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningIPV6Port = false;
            }
        }
    }


    private volatile boolean isRunningIsIPV6 = false;
    @Scheduled(fixedDelay = 180_000)
    public void isIpv6() {
        if(flag && !isRunningIsIPV6){
            isRunningIsIPV6 = true;
            try {
                Long time = System.currentTimeMillis();
                log.info("IsIpv6 Start......");
                try {
                    gatherService.gatherIsIpv6(DateTools.gatherDate());
                } catch (Exception e) {
                    log.error("Error occurred during isIpv6", e);
                }
                log.info("IsIpv6 End......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningIsIPV6 = false;
            }
        }
    }

    // 采集流量
//    @Scheduled(cron = "0 */3 * * * ?")
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


//    private volatile boolean isRunningPing = false;
//    @Scheduled(fixedDelay = 60 * 1000) // 30秒间隔，严格串行
//    public void pingSubnet() {
//        if(flag && !isRunningPing){
//            isRunningPing = true;
//            try {
//                Long time = System.currentTimeMillis();
//                log.info("ping subnet Start......");
//                try {
//                    this.subnetService.pingSubnet();
//                } catch (Exception e) {
//                    log.error("Error occurred during Subnet", e);
//                }
//                log.info("ping subnet End......" + (System.currentTimeMillis()-time));
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                isRunningPing = false;
//            }
//        }
//    }


    // TODO 已同步|待增加并发采集
    private volatile boolean isRunningSnmpStataus = false;
    @Scheduled(fixedDelay = 60 * 1000) // 30秒间隔，严格串行
    public void snmpStatus() {
        if(flag && !isRunningSnmpStataus){
            isRunningSnmpStataus = true;
            try {
                Long time = System.currentTimeMillis();
                log.info("Snmp status start......");
                try {
                     deviceManager.saveAvailableDevicesToRedis();
                } catch (Exception e) {
                    log.error("Error occurred during SNMP", e);
                }
                log.info("Snmp status end......" + (System.currentTimeMillis()-time));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningSnmpStataus = false;
            }
        }
    }



    private volatile boolean isRunningProbe = false;
    @Scheduled(fixedDelay = 180_000)
    public void probe() {
        if(flag && !isRunningProbe){
            isRunningProbe = true;
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRunningProbe = false;
            }
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

    @Scheduled(cron = "0 0/5 * * * ?")
    public Result getTraffic() {
        List<String> trafficResults = new ArrayList<>();

        try {
            List<UnitSubnet> unitSubnets = terminalUnitMapper.selectAll();
            List<Integer> vlanList = unitSubnets.stream()
                    .map(UnitSubnet::getVlan) // 提取 VLAN 字段
                    .collect(Collectors.toList());

            // 记录统一的时间戳
            Date currentTime = new Date();

            // 使用并行流处理每个 VLAN ID 获取流量数据
            ConcurrentHashMap<Integer, TrafficData> trafficDataMap = vlanList.parallelStream()
                    .map(vlanId -> {
                        TrafficData trafficData = getTraffic(vlanId);
                        return new AbstractMap.SimpleEntry<>(vlanId, trafficData);
                    })
                    .collect(Collectors.toConcurrentMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (existing, replacement) -> existing,  // 合并冲突的值
                            ConcurrentHashMap::new // 指定 ConcurrentHashMap 类型
                    ));

            // 构建结果字符串
            trafficDataMap.forEach((vlanId, trafficData) -> {
                String result = String.format("Traffic result for VLAN ID %d: IPv4 Input Rate: %s, IPv4 Output Rate: %s, IPv6 Input Rate: %s, IPv6 Output Rate: %s",
                        vlanId, trafficData.getIpv4InputRate(), trafficData.getIpv4OutputRate(), trafficData.getIpv6InputRate(), trafficData.getIpv6OutputRate());
                trafficResults.add(result);
                // 保存流量数据时使用统一的时间戳
                saveTrafficData(trafficData, currentTime);
            });

            return ResponseUtil.ok(trafficResults); // 返回所有流量数据结果
        } catch (Exception e) {
            return ResponseUtil.error("Failed to retrieve data: " + e.getMessage());
        }
    }

    private void saveTrafficData(TrafficData trafficData, Date currentTime) {
        trafficData.setAddTime(currentTime); // 使用统一的时间戳

        try {
            trafficDataMapper.insertTrafficData(trafficData);
            System.out.println("Traffic data saved for VLAN ID " + trafficData.getVlanId());
        } catch (Exception e) {
            System.err.println("Error saving traffic data for VLAN ID " + trafficData.getVlanId() + ": " + e.getMessage());
        }
    }

    public TrafficData getTraffic(int vlanId) {
        StringBuilder result = new StringBuilder();
        Session session = null;
        ChannelExec channel = null;

        try {
            session = SnmpHelper.createSession(); // 创建会话
            String command = String.format("cat vlan%d.txt", vlanId) ;

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            // 获取命令输出
            channel.connect();

            InputStream in = channel.getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }

        } catch (Exception e) {
            System.err.println("Error executing command for VLAN ID " + vlanId + ": " + e.getMessage());
            return createDefaultTrafficData(vlanId);
        } finally {
            // 确保正确关闭资源
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        // 解析并保存流量数据
        return parseTrafficData(result.toString(), vlanId);
    }

    public TrafficData parseTrafficData(String output, int vlanId) {
        String ipv4InputRate = "0";
        String ipv4OutputRate = "0";
        String ipv6InputRate = "0";
        String ipv6OutputRate = "0";

        String[] lines = output.split("\n");
        boolean isIPv4Section = false;
        boolean isIPv6Section = false;

        for (String line : lines) {
            if (line.contains("Ipv4:")) {
                isIPv4Section = true;
                isIPv6Section = false;
            } else if (line.contains("Ipv6:")) {
                isIPv6Section = true;
                isIPv4Section = false;
            }

            // 提取输入速率
            if (isIPv4Section && line.contains("Last 300 seconds input rate")) {
                ipv4InputRate = extractRate(line);
            } else if (isIPv6Section && line.contains("Last 300 seconds input rate")) {
                ipv6InputRate = extractRate(line);
            }

            // 提取输出速率
            if (isIPv4Section && line.contains("Last 300 seconds output rate")) {
                ipv4OutputRate = extractRate(line);
            } else if (isIPv6Section && line.contains("Last 300 seconds output rate")) {
                ipv6OutputRate = extractRate(line);
            }
        }
        // 返回流量数据对象
        TrafficData trafficData = new TrafficData();
        trafficData.setVlanId(vlanId);
        trafficData.setIpv4InputRate(ipv4InputRate);
        trafficData.setIpv4OutputRate(ipv4OutputRate);
        trafficData.setIpv6InputRate(ipv6InputRate);
        trafficData.setIpv6OutputRate(ipv6OutputRate);
        return trafficData;
    }

    private String extractRate(String line) {
        String[] parts = line.split(" ");
        return parts[parts.length - 4]; // 获取速率
    }
    private TrafficData createDefaultTrafficData(int vlanId) {
        TrafficData trafficData = new TrafficData();
        trafficData.setVlanId(vlanId);
        trafficData.setIpv4InputRate("0");
        trafficData.setIpv4OutputRate("0");
        trafficData.setIpv6InputRate("0");
        trafficData.setIpv6OutputRate("0");
        return trafficData;
    }

}
