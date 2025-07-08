package com.metoo.nrsm.core.manager;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.PortTrafficDataMapper;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.IFlowStatisticsService;
import com.metoo.nrsm.core.service.IFluxConfigService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.wsapi.utils.Md5Crypt;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.FluxConfig;
import com.metoo.nrsm.entity.PortTrafficData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/admin/flux/config")
@Slf4j
public class FluxConfigManagerController {

    @Autowired
    private IFluxConfigService fluxConfigService;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;

    @Autowired
    private PortTrafficDataMapper portTrafficDataMapper;


    @GetMapping("/flow2")
    public void flow2() {
        Map params = new HashMap();
        params.put("startOfDay", DateTools.getStartOfDay());
        params.put("endOfDay", DateTools.getEndOfDay());
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);

        // 2. 生成所有 5 分钟间隔的时间点
        List<Date> allTimeSlots = generate5MinuteTimeSlots();

        // 3. 补全缺失的数据
        List<FlowStatistics> completeData = new ArrayList<>();
        for (Date timeSlot : allTimeSlots) {
            Optional<FlowStatistics> matchingData = flowStatisticsList.stream()
                    .filter(data -> DateUtils.truncate(data.getAddTime(), Calendar.MINUTE).equals(DateUtils.truncate(timeSlot, Calendar.MINUTE)))
                    .findFirst();

            if (matchingData.isPresent()) {
                completeData.add(matchingData.get());
            } else {
                completeData.add(createDefaultFlowStatistics(timeSlot));
            }
        }
        System.out.println(completeData);
    }

    // 生成当天所有 5 分钟间隔的时间点
    private List<Date> generate5MinuteTimeSlots() {
        List<Date> timeSlots = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateTools.getStartOfDay());
        // 清除秒和毫秒
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        while (calendar.getTime().before(DateTools.getEndOfDay())) {
            timeSlots.add(calendar.getTime());
            calendar.add(Calendar.MINUTE, 5);
        }

        return timeSlots;
    }

    // 创建默认的流量数据（IPv4/IPv6 设为 0）
    private FlowStatistics createDefaultFlowStatistics(Date time) {
        FlowStatistics defaultData = new FlowStatistics();
        defaultData.setAddTime(time);
        defaultData.setIpv4(BigDecimal.ZERO);
        defaultData.setIpv6(BigDecimal.ZERO);
        defaultData.setIpv6Rate(BigDecimal.ZERO);
        return defaultData;
    }


    @PostMapping("/test")
    public Result test(@RequestBody FluxConfig fluxConfig) {
        Map<String, String> portMap = null;
        for (String ip : fluxConfig.getIpList()) {
            // 1. 构建SNMP参数
            SNMPV3Params snmpParams = buildSnmpParams(fluxConfig, ip);
            // 2. 获取设备端口信息
            portMap = getDevicePorts(snmpParams);
            if (portMap.isEmpty()) {
                log.warn("设备 {} IP:{} 端口信息获取失败，跳过此设备", fluxConfig.getName(), ip);
                continue;
            }
            if (!portMap.isEmpty()) {
                break;
            }
        }
        return ResponseUtil.ok(portMap);
    }

    @GetMapping
    public Result all() {
        List<FluxConfig> fluxConfigList = this.fluxConfigService.selectObjByMap(null);
        if (fluxConfigList.size() > 0) {
            for (FluxConfig fluxConfig : fluxConfigList) {
                List<List<String>> v4_oids = JSONObject.parseObject(fluxConfig.getIpv4Oid(), List.class);
                List<List<String>> v6_oids = JSONObject.parseObject(fluxConfig.getIpv6Oid(), List.class);
                fluxConfig.setIpv4Oids(v4_oids);
                fluxConfig.setIpv6Oids(v6_oids);
            }
        }
        return ResponseUtil.ok(fluxConfigList);
    }

    // 添加解析 IP 列表的方法
    private List<String> parseIpList(String ips) {
        if (ips == null || ips.isEmpty()) {
            return Collections.emptyList();
        }

        // 尝试解析为 JSON 数组
        try {
            if (ips.startsWith("[")) {
                return JSON.parseArray(ips, String.class);
            }
        } catch (Exception e) {
            // 解析失败，可能不是 JSON 格式
        }

        // 尝试按逗号分割
        if (ips.contains(",")) {
            return Arrays.asList(ips.split("\\s*,\\s*"));
        }

        // 否则作为单个 IP
        return Collections.singletonList(ips);
    }

    @PostMapping
    public Object save(@RequestBody FluxConfig fluxConfig) {
        if (fluxConfig == null) {
            return ResponseUtil.badArgument("参数错误");
        }
        // 验证 IP 列表中的每个 IP 地址
        if (fluxConfig.getIps() != null && !fluxConfig.getIps().isEmpty()) {
            // 尝试解析为 IP 列表
            List<String> ipList = parseIpList(fluxConfig.getIps());

            // 遍历解析后的 IP 列表
            for (String ip : ipList) {
                if (StringUtils.isBlank(ip)) {
                    continue;
                }

                /*if (Ipv4Util.verifyIp(ip)) {
                    // IPv4 地址，格式正确
                } else if (Ipv6Util.verifyIpv6(ip)) {
                    // IPv6 地址，格式正确
                } else {
                    return ResponseUtil.badArgument("IP 格式错误: " + ip);
                }*/
            }
        }

        if (fluxConfig.getIpv4Oids() != null) {
            String ipv4oid = JSONObject.toJSONString(fluxConfig.getIpv4Oids());
            fluxConfig.setIpv4Oid(ipv4oid);
        }

        if (fluxConfig.getIpv6Oids() != null) {
            String ipv6oid = JSONObject.toJSONString(fluxConfig.getIpv6Oids());
            fluxConfig.setIpv6Oid(ipv6oid);
        }

        if (fluxConfig.getId() != null) {
            FluxConfig obj = this.fluxConfigService.selectObjById(fluxConfig.getId());
            fluxConfig.getIpv4Oids().clear();
            fluxConfig.getIpv6Oids().clear();
            boolean flag = Md5Crypt.getDiffrent(obj, fluxConfig);
            if (!flag) {
                fluxConfig.setUpdate(1);
            }
        }
        boolean flag = this.fluxConfigService.save(fluxConfig);
        if (flag) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @DeleteMapping
    public Object delete(String ids) {
        if (ids != null && !ids.equals("")) {
            for (String id : ids.split(",")) {
                Map params = new HashMap();
                params.put("id", Long.parseLong(id));
                List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(params);
                if (fluxConfigs.size() > 0) {
                    FluxConfig fluxConfig = fluxConfigs.get(0);
                    try {
                        boolean i = this.fluxConfigService.delete(Long.parseLong(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return ResponseUtil.badArgument(fluxConfig.getName() + "删除失败");
                    }
                } else {
                    return ResponseUtil.badArgument();
                }
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }



  /*  @GetMapping("/gather")
    public void gather(){
        List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(null);
        if(fluxConfigs.size() > 0){
            BigDecimal ipv4Sum = new BigDecimal(0);
            BigDecimal ipv6Sum = new BigDecimal(0);

            BigDecimal ipv6Rate = new BigDecimal(0);

            // 获取全部ipv4流量
            List<Map<String, String>> v4_list = new ArrayList<>();
            List<Map<String, String>> v6_list = new ArrayList<>();
            for (FluxConfig fluxConfig : fluxConfigs) {
                // 获取ipv4流量
                // 1. 遍历oid
                List<List<String>> v4_oids =JSONObject.parseObject(fluxConfig.getIpv4Oid(), List.class);
                if(v4_oids.size() > 0){
                    for (List<String> oid : v4_oids) {
                        if(oid.size() > 0){
                            String in = String.valueOf(oid.get(0));
                            String out = String.valueOf(oid.get(1));
                            String result = exec_v4(fluxConfig.getIpv4(), in, out);
                            if(StringUtils.isNotEmpty(result)){
                                Map map = JSONObject.parseObject(result, Map.class);
                                v4_list.add(map);
                            }
                        }
                    }
                }

                List<List<String>> v6_oids =JSONObject.parseObject(fluxConfig.getIpv4Oid(), List.class);
                if(v6_oids.size() > 0){
                    for (List<String> oid : v6_oids) {
                        if(oid.size() > 0){
                            String in = String.valueOf(oid.get(0));
                            String out = String.valueOf(oid.get(1));
                            String result = exec_v6(fluxConfig.getIpv4(), in, out);
                            if(StringUtils.isNotEmpty(result)){
                                Map map = JSONObject.parseObject(result, Map.class);
                                v6_list.add(map);
                            }
                        }
                    }
                }
            }

            if(v4_list.size() > 0){
                BigDecimal in = v4_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal out = v4_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
                ipv4Sum = in.add(out);
                System.out.println(ipv4Sum);
            }

            if(v6_list.size() > 0){
                BigDecimal in = v6_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal out = v6_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
                ipv6Sum = in.add(out);
                System.out.println(ipv6Sum);
            }
            // ipv6流量占比=ipv6流量/（ipv4流量+ipv6流量）
            ipv6Rate = ipv6Sum.divide(ipv4Sum.add(ipv6Sum));

        }
    }

    public String exec_v4(String ip, String in, String out) {
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {ip, "v2c",
                "public@123", in, out};
        SSHExecutor sshExecutor = new SSHExecutor();
        String result = sshExecutor.exec(path, params);
        if(StringUtil.isNotEmpty(result)){
            return result;
        }
        return null;
    }

    public String exec_v6(String ip, String in, String out) {
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {ip, "v2c",
                "public@123", in, out};
        SSHExecutor sshExecutor = new SSHExecutor();
        String result = sshExecutor.exec(path, params);
        if(StringUtil.isNotEmpty(result)){
            return result;
        }
        return null;
    }*/


/*
    @GetMapping("/gather")
    public void gather() {
        List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(null);
        if (!fluxConfigs.isEmpty()) {
            BigDecimal ipv4Total = BigDecimal.ZERO;
            BigDecimal ipv6Total = BigDecimal.ZERO;
            SNMPV3Params snmpParams=null;

            for (FluxConfig config : fluxConfigs) {
                if(config.getVersion().equals("v3")){
                    // 1. 构建SNMP参数
                    snmpParams = new SNMPV3Params.Builder()
                            .version(config.getVersion())
                            .host(config.getIpv4())
                            .port(config.getPort())
                            .community(config.getCommunity())
                            .username(config.getSecurityName())
                            .securityLevel(config.getSecurityLevel())
                            .authProtocol(config.getAuthProtocol())
                            .authPassword(config.getAuthPassword())
                            .privProtocol(config.getPrivProtocol())
                            .privPassword(config.getPrivPassword())
                            .build();
                }else {
                    // 1. 构建SNMP参数
                    snmpParams = new SNMPV3Params.Builder()
                            .version(config.getVersion())
                            .host(config.getIpv4())
                            .port(config.getPort())
                            .community(config.getCommunity())
                            .build();
                }
                List<List<String>> ipv4Oids = parseOidConfig(config.getIpv4Oid());
                // 2. 采集IPv4流量
                for (List<String> oidPair : ipv4Oids) {
                    Map<String, BigDecimal> traffic = getTraffic(snmpParams, oidPair.get(0), oidPair.get(1));
                    ipv4Total = ipv4Total.add(traffic.get("in")).add(traffic.get("out"));
                }

                // 3. 采集IPv6流量
                List<List<String>> ipv6Oids = parseOidConfig(config.getIpv6Oid());
                for (List<String> oidPair : ipv6Oids) {
                    Map<String, BigDecimal> traffic = getTraffic(snmpParams, oidPair.get(0), oidPair.get(1));
                    ipv6Total = ipv6Total.add(traffic.get("in")).add(traffic.get("out"));
                }
            }

            // 4. 计算IPv6流量占比
            BigDecimal totalTraffic = ipv4Total.add(ipv6Total);
            BigDecimal ipv6Rate = BigDecimal.ZERO;
            if (totalTraffic.compareTo(BigDecimal.ZERO) != 0) {
                ipv6Rate = ipv6Total.divide(totalTraffic, 4, RoundingMode.HALF_UP);
            }

            log.info("IPv4总流量: {} GB", ipv4Total);
            log.info("IPv6总流量: {} GB", ipv6Total);
            log.info("IPv6流量占比: {}%", ipv6Rate.multiply(BigDecimal.valueOf(100)));
        }
    }

    */

    /**
     * 获取单个端口的流量数据
     *//*

    private Map<String, BigDecimal> getTraffic(SNMPV3Params snmpParams, String inOid, String outOid) {
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("in", BigDecimal.ZERO);
        result.put("out", BigDecimal.ZERO);

        try {
            String response = SNMPv3Request.getTraffic(snmpParams, inOid, outOid);

            if (StringUtils.isNotEmpty(response)) {
                Map<String, String> data = JSONObject.parseObject(response, Map.class);

                BigDecimal inBytes = new BigDecimal(data.getOrDefault("in", "0"));
                BigDecimal outBytes = new BigDecimal(data.getOrDefault("out", "0"));

                // 自动选择最合适的单位
                String[] units = {"B", "KB", "MB", "GB"};
                for (int i = 0; i < units.length; i++) {
                    BigDecimal divisor = BigDecimal.valueOf(Math.pow(1024, i));

                    // 检查值是否大于1（在目标单位下）
                    if (inBytes.divide(divisor, 4, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE) >= 0 ||
                            outBytes.divide(divisor, 4, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE) >= 0) {

                        result.put("in", inBytes.divide(divisor, 4, RoundingMode.HALF_UP));
                        result.put("out", outBytes.divide(divisor, 4, RoundingMode.HALF_UP));
                        result.put("unit", BigDecimal.valueOf(i)); // 记录使用的单位索引
                        break;
                    }

                    // 如果到达最大单位GB仍小于1，则使用GB单位
                    if (i == units.length - 1) {
                        result.put("in", inBytes.divide(divisor, 4, RoundingMode.HALF_UP));
                        result.put("out", outBytes.divide(divisor, 4, RoundingMode.HALF_UP));
                        result.put("unit", BigDecimal.valueOf(i));
                    }
                }
            }
        } catch (Exception e) {
            log.error("SNMP采集失败: host={}, inOid={}, outOid={}",
                    snmpParams.getHost(), inOid, outOid, e);
        }

        return result;
    }

*/
    private static boolean isIPv4Address(String ip) {
        try {
            return InetAddress.getByName(ip) instanceof Inet4Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private static boolean isIPv6Address(String ip) {
        try {
            return InetAddress.getByName(ip) instanceof Inet6Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }


    @GetMapping("/gather")
    @Scheduled(cron = "0 */5 * * * ?")
    public Result gather() {
        if (!flag) {
            return ResponseUtil.ok();
        }
        Date now = new Date();
        List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(null);
        if (!fluxConfigs.isEmpty()) {
            // 使用字节为单位进行累计
            BigDecimal ipv4TotalBytes = BigDecimal.ZERO;
            BigDecimal ipv6TotalBytes = BigDecimal.ZERO;

            // 存储每个端口的流量数据
            List<PortTrafficData> trafficDataList = new ArrayList<>();

            for (FluxConfig config : fluxConfigs) {
                // 检查设备是否有IP
                if (CollectionUtils.isEmpty(config.getIpList())) {
                    log.warn("设备 {} 未配置IP，跳过采集", config.getName());
                    continue;
                }


                // 处理每个IP地址
                for (String ips : config.getAllIpList()) {
                    String[] parts = ips.split("/");
                    if (parts.length < 2) {
                        log.warn("无效的 IP 配置格式: {}，格式应为 IP:index", ips);
                        continue;
                    }

                    String ip = parts[0];
                    String portIndex = parts[1];

                    boolean isIPv4 = isIPv4Address(ip);
                    boolean isIPv6 = isIPv6Address(ip);

                    if (!isIPv4 && !isIPv6) {
                        log.warn("无效的 IP 地址格式: {}", ip);
                        continue;
                    }

                    // 1. 构建 SNMP 参数（使用 IP 部分）
                    SNMPV3Params snmpParams = buildSnmpParams(config, ip);

                    // 2. 创建只包含指定端口索引的 Map
                    Map<String, String> portMap = createSinglePortMap(snmpParams, portIndex);

                    if (isIPv4) {
                        // 3. 处理IPv4 OID配置
                        List<List<String>> ipv4Oids = parseOidConfig(config.getIpv4Oid());
                        for (List<String> oidTriple : ipv4Oids) {
                            if (oidTriple.size() >= 3) {
                                String inOid = oidTriple.get(0);
                                String outOid = oidTriple.get(1);
                                String indexOid = oidTriple.get(2);

                                // 获取端口流量数据
                                processPortTraffic(config, ip, snmpParams, portMap,
                                        inOid, outOid, indexOid, false, trafficDataList);

                                // 累计总流量
                                Map<String, BigDecimal> traffic = getTrafficForAllPorts(snmpParams,
                                        portMap.keySet(), inOid, outOid);
                                ipv4TotalBytes = ipv4TotalBytes.add(traffic.get("in")).add(traffic.get("out"));
                            }
                        }
                    } else if (isIPv6) {
                        // 4. 处理IPv6 OID配置
                        List<List<String>> ipv6Oids = parseOidConfig(config.getIpv6Oid());
                        for (List<String> oidTriple : ipv6Oids) {
                            if (oidTriple.size() >= 3) {
                                String inOid = oidTriple.get(0);
                                String outOid = oidTriple.get(1);
                                String indexOid = oidTriple.get(2);

                                // 获取端口流量数据
                                processPortTraffic(config, ip, snmpParams, portMap,
                                        inOid, outOid, indexOid, true, trafficDataList);

                                // 累计总流量
                                Map<String, BigDecimal> traffic = getTrafficForAllPorts(snmpParams,
                                        portMap.keySet(), inOid, outOid);
                                ipv6TotalBytes = ipv6TotalBytes.add(traffic.get("in")).add(traffic.get("out"));
                            }
                        }
                    }
                }
            }

            // 5. 保存流量数据到数据库（用于曲线图展示）
            saveTrafficData(trafficDataList, now);

            // 6. 转换为GB单位（在最终结果转换）
            BigDecimal divisor = BigDecimal.valueOf(1_000_000_000);
            BigDecimal ipv4TotalGB = ipv4TotalBytes.divide(divisor, 4, RoundingMode.HALF_UP);
            BigDecimal ipv6TotalGB = ipv6TotalBytes.divide(divisor, 4, RoundingMode.HALF_UP);

            // 7. 计算IPv6流量占比
            BigDecimal totalTraffic = ipv4TotalGB.add(ipv6TotalGB);
            BigDecimal ipv6Rate = BigDecimal.ZERO;
            if (totalTraffic.compareTo(BigDecimal.ZERO) != 0) {
                ipv6Rate = ipv6TotalGB.divide(totalTraffic, 4, RoundingMode.HALF_UP);
            }

            log.info("IPv4总流量: {} GB", ipv4TotalGB);
            log.info("IPv6总流量: {} GB", ipv6TotalGB);
            log.info("IPv6流量占比: {}%", ipv6Rate.multiply(BigDecimal.valueOf(100)));
            return ResponseUtil.ok("流量采集成功");
        } else {
            return ResponseUtil.error("采集设备不能为空");
        }
    }

    /**
     * 创建只包含指定端口索引的 Map
     */
    private Map<String, String> createSinglePortMap(SNMPV3Params snmpParams, String portIndex) {
        try {
            // 获取设备所有端口信息
            String jsonResult = SNMPv3Request.getDevicePort(snmpParams);
            if (StringUtils.isBlank(jsonResult)) {
                return Collections.emptyMap();
            }

            // 解析为完整端口映射
            Map<String, String> fullPortMap = JSONObject.parseObject(jsonResult, Map.class);

            // 创建只包含指定索引的 Map
            String portName = fullPortMap.get(portIndex);
            if (portName != null) {
                Map<String, String> singlePortMap = new HashMap<>();
                singlePortMap.put(portIndex, portName);
                return singlePortMap;
            }
        } catch (Exception e) {
            log.error("获取端口信息失败", e);
        }
        return Collections.emptyMap();
    }


    /**
     * 处理单个端口的流量数据（参数顺序修正）
     */
    private void processPortTraffic(FluxConfig config,
                                    String ipAddress,
                                    SNMPV3Params snmpParams,
                                    Map<String, String> portMap,
                                    String inBaseOid,
                                    String outBaseOid,
                                    String indexBaseOid,
                                    boolean isIPv6,
                                    List<PortTrafficData> trafficDataList) {
        try {
            // 获取当前时间（精确到秒）
            long timestamp = System.currentTimeMillis() / 1000 * 1000; // 整秒时间戳

            // 遍历所有端口
            for (Map.Entry<String, String> entry : portMap.entrySet()) {
                String portIndex = entry.getKey();
                String portName = entry.getValue();

                // 获取当前端口的流量值
                Map<String, BigDecimal> traffic = getTrafficForPort(snmpParams, portIndex, inBaseOid, outBaseOid);

                // 计算5分钟增量（需要 config 和 portMap）
                PortTrafficData trafficData = calculateTrafficDelta(
                        config,
                        ipAddress,
                        portMap,
                        portIndex,
                        timestamp,
                        traffic.get("in"),
                        traffic.get("out"),
                        isIPv6
                );

                if (trafficData != null) {
                    trafficDataList.add(trafficData);
                }
            }
        } catch (Exception e) {
            log.error("处理端口流量失败: inOid={}, outOid={}, indexOid={}",
                    inBaseOid, outBaseOid, indexBaseOid, e);
        }
    }

    /**
     * 获取设备端口信息
     */
    private Map<String, String> getDevicePorts(SNMPV3Params snmpParams) {
        try {
            String jsonResult = SNMPv3Request.getDevicePort(snmpParams);
            if (StringUtils.isNotBlank(jsonResult)) {
                return JSONObject.parseObject(jsonResult, Map.class);
            }
        } catch (Exception e) {
            log.error("获取设备端口信息失败", e);
        }
        return Collections.emptyMap();
    }

    /**
     * 获取单个端口的当前流量值
     */
    private Map<String, BigDecimal> getTrafficForPort(SNMPV3Params snmpParams, String portIndex,
                                                      String inBaseOid, String outBaseOid) {
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("in", BigDecimal.ZERO);
        result.put("out", BigDecimal.ZERO);

        try {
            // 构建完整OID
            String inOid = inBaseOid + "." + portIndex;
            String outOid = outBaseOid + "." + portIndex;

            // 获取入站流量
            BigDecimal inValue = getSnmpCounterValue(snmpParams, inOid);
            // 获取出站流量
            BigDecimal outValue = getSnmpCounterValue(snmpParams, outOid);

            result.put("in", inValue);
            result.put("out", outValue);
        } catch (Exception e) {
            log.error("获取端口流量失败: portIndex={}, inBaseOid={}, outBaseOid={}",
                    portIndex, inBaseOid, outBaseOid, e);
        }

        return result;
    }

    /**
     * 获取所有端口的流量总和
     */
    private Map<String, BigDecimal> getTrafficForAllPorts(SNMPV3Params snmpParams, Set<String> portIndexes,
                                                          String inBaseOid, String outBaseOid) {
        BigDecimal inTotal = BigDecimal.ZERO;
        BigDecimal outTotal = BigDecimal.ZERO;

        for (String portIndex : portIndexes) {
            Map<String, BigDecimal> traffic = getTrafficForPort(snmpParams, portIndex, inBaseOid, outBaseOid);
            inTotal = inTotal.add(traffic.get("in"));
            outTotal = outTotal.add(traffic.get("out"));
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("in", inTotal);
        result.put("out", outTotal);
        return result;
    }

    /**
     * 从SNMP获取计数器值
     */
    private BigDecimal getSnmpCounterValue(SNMPV3Params snmpParams, String oid) {
        try {
            // 使用 getSingleValue 获取单个计数器值
            String response = SNMPv3Request.getTraffic(snmpParams, oid, "");
            if (StringUtils.isNotBlank(response)) {
                // 解析JSON响应
                JSONObject json = JSONObject.parseObject(response);
                if (json != null) {
                    String valueStr = json.getString("in");
                    if (StringUtils.isNotBlank(valueStr) && NumberUtil.isNumber(valueStr)) {
                        return new BigDecimal(valueStr);
                    }
                }
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("获取SNMP计数器值失败: oid={}", oid, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 查询特定端口的最近一次流量记录
     */
    private PortTrafficData findLastTrafficRecord(FluxConfig config,
                                                  String ipAddress,
                                                  String portIndex,
                                                  boolean isIPv6) {
        // 1. 计算5分钟前的时间戳（秒级）
        long minTimestamp = System.currentTimeMillis() / 1000 - 360;

        // 2. 查询最近的有效记录 - 修改：使用当前ipAddress
        List<PortTrafficData> records = portTrafficDataMapper.findByDeviceAndPortAndTime(
                config.getId(),
                ipAddress,  // 使用当前IP地址
                portIndex,
                isIPv6,
                minTimestamp
        );


        return records != null && !records.isEmpty() ? records.get(0) : null;
    }

    /**
     * 计算流量增量（参数修正）
     */
    private PortTrafficData calculateTrafficDelta(FluxConfig config,
                                                  String ipAddress,  // 新增当前IP参数
                                                  Map<String, String> portMap,
                                                  String portIndex,
                                                  long timestamp,
                                                  BigDecimal currentIn,
                                                  BigDecimal currentOut,
                                                  boolean isIPv6) {
        String portName = portMap.getOrDefault(portIndex, "Unknown");

        // 查询上次记录 - 修改：使用当前ipAddress
        PortTrafficData lastRecord = findLastTrafficRecord(config, ipAddress, portIndex, isIPv6);

        // 计算增量（处理计数器回绕）
        BigDecimal inDelta = calculateSafeDelta(currentIn, lastRecord != null ? lastRecord.getInBytes() : null);
        BigDecimal outDelta = calculateSafeDelta(currentOut, lastRecord != null ? lastRecord.getOutBytes() : null);

        // 校验异常增量（过大可能是设备重启导致计数器重置）
        if (lastRecord != null) {
            // 检查异常大的增量（超过1GB的5分钟增量）
            BigDecimal maxExpectedDelta = BigDecimal.valueOf(1073741824L); // 1GB
            if (inDelta.compareTo(maxExpectedDelta) > 0 || outDelta.compareTo(maxExpectedDelta) > 0) {
                log.warn("设备 {}({}) 端口 {} 检测到异常流量增量: in={} bytes, out={} bytes",
                        config.getName(), config.getIps(), portName, inDelta, outDelta);

                // 重置增量为0
                inDelta = BigDecimal.ZERO;
                outDelta = BigDecimal.ZERO;
            }
        }

        // 创建新记录
        PortTrafficData trafficData = new PortTrafficData();
        trafficData.setDeviceId(config.getId());
        trafficData.setDeviceName(config.getName());
        trafficData.setIpAddress(ipAddress);
        trafficData.setPortIndex(portIndex);
        trafficData.setPortName(portName);
        trafficData.setTimestamp(timestamp);
        trafficData.setInBytes(currentIn);
        trafficData.setOutBytes(currentOut);
        trafficData.setInDelta(inDelta);
        trafficData.setOutDelta(outDelta);
        trafficData.setIsIpv6(isIPv6);

        return trafficData;
    }

    /**
     * 安全计算增量（处理计数器回绕）- 优化实现
     */
    private BigDecimal calculateSafeDelta(BigDecimal current, BigDecimal last) {
        if (last == null) {
            return BigDecimal.ZERO;
        }

        if (current.compareTo(last) >= 0) {
            return current.subtract(last);
        }

        // 处理计数器回绕
        BigDecimal max32 = new BigDecimal("4294967295");   // 32位最大值
        BigDecimal max64 = new BigDecimal("18446744073709551615"); // 64位最大值

        // 判断计数器类型：32位还是64位
        if (last.compareTo(max32) <= 0) {
            // 32位计数器
            return current.add(max32.subtract(last));
        } else {
            // 64位计数器
            return current.add(max64.subtract(last));
        }
    }


    /**
     * 保存流量数据到数据库
     */
    private void saveTrafficData(List<PortTrafficData> trafficDataList, Date now) {
        if (CollectionUtils.isEmpty(trafficDataList)) {
            return;
        }

        // 设置创建和更新时间

        for (PortTrafficData data : trafficDataList) {
            data.setCreateTime(now);
            data.setUpdateTime(now);
        }

        // 分批插入
        int batchSize = 500;
        for (int i = 0; i < trafficDataList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, trafficDataList.size());
            List<PortTrafficData> subList = trafficDataList.subList(i, end);
            portTrafficDataMapper.batchInsert(subList);
        }

        log.info("已保存 {} 条流量记录", trafficDataList.size());
    }

    /**
     * 构建SNMP参数
     */
    private SNMPV3Params buildSnmpParams(FluxConfig config, String ipAddress) {
        SNMPV3Params.Builder builder = new SNMPV3Params.Builder()
                .version(config.getVersion())
                .host(ipAddress)  // 使用传入的IP地址
                .port(config.getPort())
                .community(config.getCommunity());

        if ("v3".equals(config.getVersion())) {
            builder.username(config.getSecurityName())
                    .securityLevel(config.getSecurityLevel())
                    .authProtocol(config.getAuthProtocol())
                    .authPassword(config.getAuthPassword())
                    .privProtocol(config.getPrivProtocol())
                    .privPassword(config.getPrivPassword());
        }

        return builder.build();
    }

    /**
     * 解析OID配置字符串并过滤掉空数组
     */
    private List<List<String>> parseOidConfig(String oidConfig) {
        List<List<String>> result = new ArrayList<>();

        if (StringUtils.isBlank(oidConfig)) {
            return result;
        }

        try {
            // 解析JSON数组
            List<List<String>> allPairs = JSONArray.parseArray(oidConfig, List.class)
                    .stream()
                    .map(o -> (List<String>) o)
                    .collect(Collectors.toList());

            // 过滤掉空数组和无效数据
            for (List<String> oidTriple : allPairs) {
                // 跳过空数组 []
                if (oidTriple == null || oidTriple.isEmpty() || oidTriple.get(0).isEmpty()) {
                    continue;
                }

                // 确保至少有一个OID有效
                boolean isValid = false;
                List<String> validTriple = new ArrayList<>(3);

                for (int i = 0; i < Math.min(3, oidTriple.size()); i++) {
                    String oid = oidTriple.get(i);
                    if (StringUtils.isNotBlank(oid)) {
                        validTriple.add(oid.trim());
                        isValid = true;
                    } else {
                        validTriple.add(""); // 填充空字符串
                    }
                }

                if (isValid) {
                    // 确保三元组长度为3
                    while (validTriple.size() < 3) {
                        validTriple.add("");
                    }
                    result.add(validTriple);
                }
            }
        } catch (Exception e) {
            log.error("解析OID配置失败: {}", oidConfig, e);
        }

        return result;
    }

    @Value("${task.switch.is-open}")
    private boolean flag;

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cleanupOldTrafficData() {
        if (!flag) {
            return;
        }
        long thirtyDaysAgo = System.currentTimeMillis() / 1000 - 30 * 24 * 3600;
        portTrafficDataMapper.deleteByTimestampBefore(thirtyDaysAgo);
        log.info("已清理30天前的流量记录");
    }


}
