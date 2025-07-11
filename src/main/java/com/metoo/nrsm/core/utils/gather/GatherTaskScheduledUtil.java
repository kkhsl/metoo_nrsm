package com.metoo.nrsm.core.utils.gather;

import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.api.traffic.TimeUtils;
import com.metoo.nrsm.core.api.traffic.TrafficApi;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.mapper.TrafficDataMapper;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.api.ApiExecUtils;
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherSingleThreadingMacSNMPUtils;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    @Value("${task.switch.traffic.api.is-open}")
    private boolean trafficApi;
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
    private DeviceManager deviceManager;
    @Autowired
    private GatherSingleThreadingMacSNMPUtils gatherSingleThreadingMacSNMPUtils;
    @Resource
    private TerminalUnitMapper terminalUnitMapper;
    @Resource
    private TrafficDataMapper trafficDataMapper;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 任务在 00:00 启动，执行耗时 6 分钟（到 00:06）00:05 时，新任务会启动（即使前一个任务未完成）
     * 该任务必须使用当前表达式
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void api() {
        if (traffic) {
            if (lock.tryLock()) {
                try {
                    Long time = System.currentTimeMillis();
                    log.info("流量推送开始：{}", time);
                        apiExecUtils.exec();
                    log.info("流量推送结束：{}", (System.currentTimeMillis() - time));
                } catch (Exception e) {
                    log.error("流量推送失败：{}", e.getMessage());
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    @Autowired
    private TrafficApi trafficApiService;
    @Autowired
    private IFlowUnitService flowUnitService;
    @Autowired
    private ITrafficService trafficService;
    private final ReentrantLock trafficApiLock = new ReentrantLock();
    @Scheduled(cron = "0 */5 * * * ?")
    public void trafficAPI(){
        if (trafficApi) {
            if (trafficApiLock.tryLock()) {
                try {
                    LocalDateTime baseTime = TimeUtils.getNow();
                    String currentTime = TimeUtils.format(TimeUtils.clearSecondAndNano(baseTime));
                    String fiveMinutesBefore = TimeUtils.format(TimeUtils.getFiveMinutesBefore(baseTime));
                    List<FlowUnit> flowUnits = flowUnitService.selectObjByMap(Collections.emptyMap());
                    if (flowUnits == null || flowUnits.isEmpty()) {
                        log.info("未获取到任何单位，跳过调用 NetFlow API。");
                        return;
                    }
                    for (FlowUnit flowUnit : flowUnits) {

                        String unitName = flowUnit.getUnitName();
                        if (unitName == null || unitName.trim().isEmpty()) {
                            log.warn("单位名称为空，跳过该单位的流量查询。");
                            continue;
                        }

                        TrafficApi.ApiResponse response = trafficApiService.queryNetFlow(unitName, fiveMinutesBefore, currentTime);

                        if (!response.isSuccess()) {
                            log.error("调用 NetFlow API 失败，单位: {}, 错误信息: {}", unitName, response.getError());
                            continue;
                        }
                        Map<String, Object> dataMap = Optional.ofNullable(response.getData())
                                .map(d -> (Map<String, Object>) d.get("data"))
                                .orElse(null);

                        if (dataMap == null) {
                            log.warn("NetFlow API 返回空数据，单位: {}", unitName);
                            continue;
                        }
                        Traffic traffic = new Traffic();
                        traffic.setUnitName(String.valueOf(dataMap.getOrDefault("name", unitName)));
                        traffic.setVfourFlow(String.valueOf(dataMap.getOrDefault("ipv4Flow", "0.0")));
                        traffic.setVsixFlow(String.valueOf(dataMap.getOrDefault("ipv6Flow", "0.0")));
                        traffic.setAddTime(Date.from(baseTime.atZone(ZoneId.systemDefault()).toInstant()));
                        trafficService.save(traffic);
                        log.info("成功保存流量数据: 单位: {}, IPv4: {}, IPv6: {}", traffic.getUnitName(),
                                traffic.getVfourFlow(), traffic.getVsixFlow());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(trafficApiLock != null){
                        trafficApiLock.unlock();
                    }
                }
            }
        }
    }

    private volatile boolean isRunningDhcp = false;

    @Scheduled(fixedDelay = 180_000)
    public void dhcp() {
        if (flag && !isRunningDhcp) {
            log.info("DHCP采集任务开始");
            isRunningDhcp = true;
            try {
                Long time = System.currentTimeMillis();
                dhcpService.gather(DateTools.gatherDate());
                log.info("DHCP采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("DHCP采集任务异常: {}", e.getMessage());
            } finally {
                isRunningDhcp = false;
            }
        }
    }

    private volatile boolean isRunningDhcp6 = false;

    @Scheduled(fixedDelay = 180_000)
    public void dhcp6() {
        if (flag && !isRunningDhcp6) {
            log.info("DHCP6采集任务开始");
            isRunningDhcp6 = true;
            try {
                Long time = System.currentTimeMillis();
                dhcp6Service.gather(DateTools.gatherDate());
                log.info("DHCP6采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("DHCP6采集任务异常: {}", e.getMessage());
            } finally {
                isRunningDhcp6 = false;
            }
        }
    }

    private volatile boolean isRunningARP = false;

    @Scheduled(fixedDelay = 180_000)
    public void arp() {
        if (flag && !isRunningARP) {
            log.info("ARP采集任务开始");
            isRunningARP = true;
            try {
                Long time = System.currentTimeMillis();
                gatherService.gatherArp(DateTools.gatherDate());
                log.info("ARP采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("ARP采集任务异常: {}", e.getMessage());
            } finally {
                isRunningARP = false;
            }
        }
    }

    private volatile boolean isRunningTerminal = false;

    @Scheduled(fixedDelay = 180_000)
    public void gatherTerminal() {
        if (flag && !isRunningTerminal) {
            log.info("终端采集任务开始");
            isRunningTerminal = true;
            try {
                Long time = System.currentTimeMillis();
                this.gatherSingleThreadingMacSNMPUtils.updateTerminal(DateTools.gatherDate());
                log.info("终端采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("终端采集任务异常: {}", e.getMessage());
            } finally {
                isRunningTerminal = false;
            }
        }
    }

    private volatile boolean isRunningMAC = false;
    @Scheduled(fixedDelay = 180_000)
    public void gatherMac() {
        if (flag && !isRunningMAC) {
            log.info("MAC采集任务开始");
            isRunningMAC = true;
            try {
                Long time = System.currentTimeMillis();
                this.gatherService.gatherMac(DateTools.gatherDate(), new ArrayList<>());
                log.info("MAC采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("MAC采集任务异常: {}", e.getMessage());
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
        if (flag && !isRunningIPV4) {
            log.info("IPV4采集任务开始");
            isRunningIPV4 = true;
            try {
                Long time = System.currentTimeMillis();
                gatherService.gatherIpv4Thread(DateTools.gatherDate(), new ArrayList<>());
                log.info("IPV4采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("IPV4采集任务异常: {}", e.getMessage());
            } finally {
                isRunningIPV4 = false;
            }
        }
    }

    private volatile boolean isRunningIPV4Detail = false;

    @Scheduled(fixedDelay = 180_000)
    public void ipv4Detail() {
        if (flag && !isRunningIPV4Detail) {
            log.info("IPV4 detail 采集任务开始");
            isRunningIPV4Detail = true;
            try {
                Long time = System.currentTimeMillis();
                gatherService.gatherIpv4Detail(DateTools.gatherDate());
                log.info("IPV4 detail 采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("IPV4采集任务异常: {}", e.getMessage());
            } finally {
                isRunningIPV4Detail = false;
            }
        }
    }

    private volatile boolean isRunningPort = false;

    @Scheduled(fixedDelay = 180_000)
    public void port() {
        if (flag && !isRunningPort) {
            log.info("IPV4 Port采集任务开始");
            isRunningPort = true;
            try {
                Long time = System.currentTimeMillis();
                gatherService.gatherPort(DateTools.gatherDate(), new ArrayList<>());
                log.info("IPV4 Port 采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("IPV4 Port采集任务异常: {}", e.getMessage());
            } finally {
                isRunningPort = false;
            }
        }
    }


    private volatile boolean isRunningIPV6 = false;

    @Scheduled(fixedDelay = 180_000)
    public void ipv6() {
        if (flag && !isRunningIPV6) {
            log.info("Ipv6采集任务开始");
            isRunningIPV6 = true;
            try {
                Long time = System.currentTimeMillis();
                gatherService.gatherIpv6Thread(DateTools.gatherDate(), new ArrayList<>());
                log.info("Ipv6采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("IPV4 Port采集任务异常: {}", e.getMessage());
            } finally {
                isRunningIPV6 = false;
            }
        }
    }

    private volatile boolean isRunningIPV6Port = false;

    @Scheduled(fixedDelay = 180_000)
    public void portIpv6() {
        if (flag && !isRunningIPV6Port) {
            log.info("Ipv6 Port采集任务开始");
            isRunningIPV6Port = true;
            try {
                Long time = System.currentTimeMillis();
                gatherService.gatherPortIpv6(DateTools.gatherDate(), new ArrayList<>());
                log.info("Ipv6 Port采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Ipv6 Port采集任务异常: {}", e.getMessage());
            } finally {
                isRunningIPV6Port = false;
            }
        }
    }

    private volatile boolean isRunningIsIPV6 = false;

    @Scheduled(fixedDelay = 180_000)
    public void isIpv6() {
        if (flag && !isRunningIsIPV6) {
            log.info("IsIpv6采集任务开始");
            isRunningIsIPV6 = true;
            try {
                Long time = System.currentTimeMillis();
                gatherService.gatherIsIpv6(DateTools.gatherDate());
                log.info("IsIpv6采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("IsIpv6采集任务异常: {}", e.getMessage());
            } finally {
                isRunningIsIPV6 = false;
            }
        }
    }

    // 采集流量,整点
    private volatile boolean isRunningFlux = false;

    @Scheduled(cron = "0 */5 * * * ?")
    public void flux() {
        if (flag && !isRunningFlux) {
            log.info("Flux采集任务开始");
            isRunningFlux = true;
            Long time = System.currentTimeMillis();
            try {
                gatherService.gatherFlux(DateTools.gatherDate());
                log.info("Flux采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                log.error("Flux采集任务异常: {}", e.getMessage());
            } finally {
                isRunningFlux = false;
            }
        }
    }


    /**
     * 任务开始时间：T=0
     * <p>
     * 任务结束时间：T=5分钟
     * <p>
     * 下一次执行时间：T=5 + 3 = 8分钟（不会在 T=3分钟 时触发新任务）。
     * <p>
     * 更新arp缓存
     */
    private volatile boolean isRunningPing = false;

    @Scheduled(fixedDelay = 300 * 1000) // 30秒间隔，严格串行
    public void pingSubnet() {
        if (flag && !isRunningPing) {
            log.info("PING 网段采集开始===========================================================");
            isRunningPing = true;
            try {
                Long time = System.currentTimeMillis();
                this.subnetService.pingSubnet();
                log.info("PING 网段采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));

                log.info("PING 网段采集结束===========================================================");

            } catch (Exception e) {
                e.printStackTrace();
                log.error("PING 网段采集异常: {}", e.getMessage());
            } finally {
                isRunningPing = false;
            }
        }
    }


    // TODO 已同步|待增加并发采集
    private volatile boolean isRunningSnmpStataus = false;

    @Scheduled(fixedDelay = 300 * 1000) // 30秒间隔，严格串行
    public void snmpStatus() {
        if (flag && !isRunningSnmpStataus) {
            log.info("Snmp status采集开始");
            isRunningSnmpStataus = true;
            try {
                Long time = System.currentTimeMillis();
                deviceManager.saveAvailableDevicesToRedis();
                log.info("Snmp status 采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Snmp status 采集异常: {}", e.getMessage());
            } finally {
                isRunningSnmpStataus = false;
            }
        }
    }


//    private volatile boolean isRunningProbe = false;
//    @Scheduled(fixedDelay = 180_000)
//    public void probe() {
//        if(flag && !isRunningProbe){
//            log.info("Probe采集开始");
//            isRunningProbe = true;
//            try {
//                Long time = System.currentTimeMillis();
//                if(getLicenseProbe()){
//                    probeService.scanByTerminal();
//                }
//                log.info("Probe 网段采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("Probe 网段采集异常: {}", e.getMessage());
//            } finally {
//                isRunningProbe = false;
//            }
//        }
//    }

    public boolean getLicenseProbe() {
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
        if (flowStatisticsList.size() > 0) {
            BigDecimal sum = flowStatisticsList.stream().filter(e -> e.getIpv6Rate() != null).map(FlowStatistics::getIpv6Rate)
                    .collect(Collectors.toList())
                    .stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            long count = flowStatisticsList.stream().filter(e -> e.getIpv6Rate() != null).map(FlowStatistics::getIpv6Rate)
                    .collect(Collectors.toList())
                    .stream().count();

            if (sum.compareTo(new BigDecimal(0)) >= 1) {
                BigDecimal rate = sum.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                fluxDailyRate.setRate(rate);
                GradeWeight gradeWeight = this.gradWeightService.selectObjOne();
                if (gradeWeight != null) {
                    if (gradeWeight.getReach() != null && gradeWeight.getReach().compareTo(new BigDecimal(0)) >= 1) {
                        if (rate.compareTo(gradeWeight.getReach()) > -1) {
                            fluxDailyRate.setFlag(true);
                        }
                    }
                }
            }
        }
        this.fluxDailyRateService.save(fluxDailyRate);
    }

    //    @Scheduled(cron = "0 0/5 * * * ?")
    @Scheduled(fixedDelay = 60_000)
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
            String command = String.format("cat vlan%d.txt", vlanId);

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
