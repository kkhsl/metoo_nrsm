package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherSingleThreadingMacSNMPUtils;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMultithreadingMacUtils;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherSingleThreadingMacUtils;
import com.metoo.nrsm.core.utils.gather.thread.*;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.snmp4j.security.SecurityLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 10:29
 */
@Slf4j
@Service
public class GatherServiceImpl implements IGatherService {

    @Autowired
    private IArpService arpService;
    @Autowired
    private Ipv4Service ipv4Service;
    @Autowired
    private Ipv6Service ipv6Service;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IFluxConfigService fluxConfigService;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;
    @Autowired
    private PythonExecUtils pythonExecUtils;
    @Autowired
    private IPingService pingService;
    @Autowired
    private ISubnetService subnetService;
    @Autowired
    private SnmpStatusUtils snmpStatusUtils;
    @Autowired
    private GatherSingleThreadingMacUtils gatherSingleThreadingMacUtils;
    @Autowired
    private GatherSingleThreadingMacSNMPUtils gatherSingleThreadingMacSNMPUtils;
    @Autowired
    private GatherMultithreadingMacUtils gatherMultithreadingMacUtils;
    @Autowired
    private Ipv4DetailService ipV4DetailService;
    @Autowired
    private IPortService portService;
    @Autowired
    private IPortIpv6Service portIpv6Service;
    @Autowired
    private GatherDataThreadPool gatherDataThreadPool;


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

    @Override
    public Map gatherMac(Date date, List<NetworkElement> networkElements) {
        if(networkElements.size() <= 0){
            networkElements = this.getGatherDevice();
        }
        if(networkElements.size() > 0){
//            Map log = gatherSingleThreadingMacUtils.gatherMac(networkElements, date);
            Map log = gatherSingleThreadingMacSNMPUtils.gatherMac(networkElements, date);

            return log;
        }
        return new HashMap();
    }

    @Override
    public void gatherMacThread(Date date) {
        List<NetworkElement> networkElements = this.getGatherDevice();
        this.gatherMultithreadingMacUtils.gatherMacThread(networkElements, date);
    }

    @Override
    public void gatherArp(Date date) {
        List<NetworkElement> networkElements = this.getGatherDevice();
        if(networkElements.size() > 0) {
//            // 步骤一 清空采集表
//            this.arpService.truncateTableGather();
//            // 清空数据之后的插入失败（测试清空表后，等待3秒）
////            try {
////                Thread.sleep(3000);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//
////            // 步骤二 合并mac和port相同的数据（ipv4和ipv6）到arp表
////            this.mergeIpv4AndIpv6ToArpGather_sql_insert(date);
////
////            // 步骤三 排除上个步骤中的数据，写入arp表
////            this.arpService.writeArp();
//
//            // 步骤 二和三合并
//            this.batchSaveIpV4AndIpv6ToArpGather(date);
//
//            // 步骤四 复制数据到arp表（待测试：使用delete 或 truncate ）
//            this.copyGatherDataToArp();

            // 合并一二三四步骤（以上步骤可完成arp数据，bug：自动采集时没有ipv6需等待N秒）
            // 合并后使用存储过程（性能待测试）
            this.arpService.gatherArp(date);
        }
    }

    public void mergeIpv4AndIpv6ToArpGather_sql(Date date){
        Map params = new HashMap();
        params.put("addTime", date);
        List<Arp> arps = this.arpService.mergeIpv4AndIpv6(params);
        if(arps.size() > 0){
//            arps.stream().forEach(e -> e.setAddTime(date));
            this.arpService.batchSaveGather(arps);
        }
    }

    public void mergeIpv4AndIpv6ToArpGather_sql_insert(Date date){
        Map params = new HashMap();
        params.put("addTime", date);
        try {
            this.arpService.batchSaveGatherBySelect(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mergeIpv4AndIpv6ToArpGather_code(Date date){
        List<Arp> arps = this.arpService.joinSelectObjAndIpv6();
        if(arps.size() > 0) {
            for (Arp arp : arps) {
                arp.setAddTime(date);
                List<Ipv6> ipv6s = arp.getIpv6List();
                if (ipv6s.size() > 0) {
                    if (ipv6s.size() == 1) {
                        arp.setV6ip(ipv6s.get(0).getIp());
                    } else {
                        for (int i = 0; i < ipv6s.size(); i++) {
                            String v6ip = "v6ip";
                            if(i > 0){
                                v6ip = "v6ip" + i;
                            }
                            Field[] fields = Arp.class.getDeclaredFields();
                            for (Field field : fields) {
                                String propertyName = field.getName(); // 获取属性名

                                // 设置属性值为"Hello World!"
                                if (v6ip.equalsIgnoreCase(propertyName)) {
                                    field.setAccessible(true); // 若属性为private或protected需要先调用此方法进行访问控制的关闭
                                    try {
                                        field.set(arp, ipv6s.get(i).getIp());
                                        break;
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.arpService.batchSaveGather(arps);
        }
    }

    public void batchSaveIpV4AndIpv6ToArpGather(Date date){
        Map params = new HashMap();
        params.put("addTime", date);
        this.arpService.batchSaveIpV4AndIpv6ToArpGather(params);
    }

    // 复制采集数据到ipv4表
    public void copyGatherDataToArp(){
        try {
            this.arpService.copyGatherDataToArp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gatherIpv4(Date date) {
        List<NetworkElement> networkElements = this.getGatherDevice();
        if(networkElements.size() > 0) {
            this.ipv4Service.truncateTableGather();
            for (NetworkElement networkElement : networkElements) {
                String path = Global.PYPATH + "getarp.py";

                if(StringUtils.isBlank(networkElement.getVersion())
                        || StringUtils.isBlank(networkElement.getCommunity())){
                    continue;
                }

                String[] params = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String result = pythonExecUtils.exec(path, params);
                if(!"".equals(result)){
                    try {
                        List<Ipv4> ipv4s = JSONObject.parseArray(result, Ipv4.class);
                        if(ipv4s.size()>0){
                            ipv4s.forEach(e -> {
                                e.setDeviceIp(networkElement.getIp());
                                e.setDeviceName(networkElement.getDeviceName());
                                e.setAddTime(date);
//                                this.ipv4Service.saveGather(e);
                            });
                            this.ipv4Service.batchSaveGather(ipv4s);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            // 非多线程，单线程串行情况下可放到最后执行(提到前面？)
            this.ipv4Service.clearAndcopyGatherDataToIpv4();
            this.removeDuplicatesIpv4();
        }
    }

    @Override
    public Map gatherIpv4Thread(Date date, List<NetworkElement> networkElements) {

        Map logMessages = new LinkedHashMap();

        if(networkElements.size() <= 0){
            networkElements = this.getGatherDevice();
        }

        if(networkElements.size() > 0) {
            // （采集结束，复制到ipv4表中）
            // 多线程并行执行，避免出现采集未结束时，执行copy操作没有数据，所以将复制数据操作（不会出现空表操作，放到最后即可，避免放在前面数据非实时数据）
            // 放到采集前，复制上次采集结果即可
            this.ipv4Service.clearAndcopyGatherDataToIpv4();

            // 移除重复项-arp采集时，使用metoo_ipv4_duplicates表
            this.removeDuplicatesIpv4();

            this.ipv4Service.truncateTableGather();

            CountDownLatch latch = new CountDownLatch(networkElements.size());

            int count = 0;
            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion())
                        || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    logMessages.put("IPv4 ARP：" + networkElement.getIp(), "设备信息异常");
                    continue;
                }
                count++;
                logMessages.put("IPv4 ARP：" + networkElement.getIp(), "采集完成");

                gatherDataThreadPool.execute(new GatherIPv4SNMPRunnable(networkElement, date, latch));

            }

            try {

                boolean completed = latch.await(10, TimeUnit.MINUTES);
                log.info("采集结果：{}", completed ? "COMPLETED" : "TIMEOUT");

                log.info("处理完成，线程池状态: {}", gatherDataThreadPool.getPoolStatus());

                logMessages.put("MAC 采集总数", count);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("处理被中断");
            }
        }
        return logMessages;
    }

    @Override
    public void gatherIpv4Detail(Date date) {
        // 将ip数据写入到metoo_ip_detail表，提供到网段梳理中（可以单独做一个ip的采集动作，如果为了保证数据的一致性，可以放到ipv4采集中）
        // 使用去重后的数据
        Map params = new HashMap();
        params.clear();
        List<Ipv4> ipv4List = this.ipv4Service.selectDuplicatesObjByMap(params);
        if(ipv4List.size() > 0){
            Ipv4Detail ipv4DetailInit = this.ipV4DetailService.selectObjByIp("0.0.0.0");
            ipv4DetailInit.setTime(ipv4DetailInit.getTime() + 1);
            this.ipV4DetailService.update(ipv4DetailInit);

            List<String> ips = new ArrayList<>();

            for (Ipv4 ipv4 : ipv4List) {
                ips.add(Ipv4Util.ipConvertDec(ipv4.getIp()));
                if(StringUtils.isNotBlank(ipv4.getIp())){
                    Ipv4Detail ipv4Detail = this.ipV4DetailService.selectObjByIp(Ipv4Util.ipConvertDec(ipv4.getIp()));
                    if(ipv4Detail == null){
                        Ipv4Detail instance = new Ipv4Detail();
                        instance.setAddTime(date);
                        instance.setMac(ipv4.getMac());
                        instance.setDeviceName(ipv4.getDeviceName());
                        instance.setOnline(true);
                        instance.setIp(Ipv4Util.ipConvertDec(ipv4.getIp()));

                        instance.setTime(instance.getTime() + 1);
                        int initTime = ipv4DetailInit.getTime() + 1;
                        float num = (float) instance.getTime() / initTime;
                        int usage = Math.round(num * 100);
                        instance.setUsage(usage);

                        this.ipV4DetailService.save(instance);
                    }else{
                        ipv4Detail.setAddTime(date);
                        ipv4Detail.setMac(ipv4.getMac());
                        ipv4Detail.setDeviceName(ipv4.getDeviceName());
                        ipv4Detail.setOnline(true);
                        ipv4Detail.setIp(Ipv4Util.ipConvertDec(ipv4.getIp()));
                        ipv4Detail.setTime(ipv4Detail.getTime() + 1);
                        int initTime = ipv4DetailInit.getTime() + 1;
                        float num = (float) ipv4Detail.getTime() / initTime;
                        int usage = Math.round(num * 100);
                        ipv4Detail.setUsage(usage);

                        this.ipV4DetailService.update(ipv4Detail);
                    }
                }
            }
//                 设置离线
            if(ips.size() > 0){
                params.clear();
                params.put("notId", ipv4DetailInit.getId());
                params.put("notIps", ips);
                List<Ipv4Detail> ipv4DetailsList = this.ipV4DetailService.selectObjByMap(params);
                if(ipv4DetailsList.size() > 0){
                    for (Ipv4Detail ipv4Detail : ipv4DetailsList) {
                        ipv4Detail.setAddTime(date);
                        ipv4Detail.setOnline(false);
                        ipv4Detail.setIp(Ipv4Util.ipConvertDec(ipv4Detail.getIp()));
                        this.ipV4DetailService.update(ipv4Detail);
                    }
                }
            }
        }
    }

    // 去重ipv4重复数据到ipv4_mirror(组合arp表)
    public void removeDuplicatesIpv4(){
        try {
            // 去重（将采集到的数据，复制并创创建相同结构的表中，并去除重复数据（mac + ip），使用存储过程完成）
            this.ipv4Service.removeDuplicates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map gatherIpv6(Date date, List<NetworkElement> networkElements) {

        Map logMessages = new LinkedHashMap();

        if(networkElements.size() <= 0){
            networkElements = this.getGatherDevice();
        }

        if(networkElements.size() > 0) {

            this.ipv6Service.truncateTableGather();
            int count = 0;
            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    logMessages.put("IPv6 ARP：" + networkElement.getIp(), "设备信息异常");
                    continue;
                }
                count++;
                String path = Global.PYPATH +  "getarpv6.py";
                String[] params2 = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String result = pythonExecUtils.exec(path, params2);
                if(!"".equals(result)){
                    try {
                        List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
                        if(array.size()>0){
                            array.forEach(e -> {
                                e.setDeviceIp(networkElement.getIp());
                                e.setDeviceName(networkElement.getDeviceName());
                                e.setAddTime(date);
                            });
                        }
                        this.ipv6Service.batchSaveGather(array);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                logMessages.put("IPv6 ARP：" + networkElement.getIp(), "采集完成");

            }
            logMessages.put("IPv6 ARP 采集总数", count);
            // 非多线程，单线程串行情况下可放到最后执行
            this.copyGatherDataToIpv6();
            this.removeDuplicatesIpv6();
        }
        return logMessages;
    }


    @Override
    public Map gatherIpv6Thread(Date date, List<NetworkElement> networkElements) {
        Map logMessages = new LinkedHashMap();
        if(networkElements.size() <= 0){
            networkElements = this.getGatherDevice();
        }

        if(networkElements.size() > 0) {

            this.copyGatherDataToIpv6();

            this.removeDuplicatesIpv6();

            this.ipv6Service.truncateTableGather();

            CountDownLatch latch = new CountDownLatch(networkElements.size());
            int count = 0;
            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    logMessages.put("IPv6 ARP：" + networkElement.getIp(), "设备信息异常");
                    continue;
                }
                count++;

                gatherDataThreadPool.execute(new GatherIpV6SNMPRunnable(networkElement, date, latch));

                logMessages.put("IPv6 ARP：" + networkElement.getIp(), "采集完成");
            }
            try {

                boolean completed = latch.await(10, TimeUnit.MINUTES);
                log.info("采集结果：{}", completed ? "COMPLETED" : "TIMEOUT");

                log.info("处理完成，线程池状态: {}", gatherDataThreadPool.getPoolStatus());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("处理被中断");
            }
        }
        return logMessages;
    }

    // 复制采集数据到ipv6表
    public void copyGatherDataToIpv6(){
        try {
            this.ipv6Service.copyGatherToIpv6();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 去重ipv4重复数据，组合arp表
    public void removeDuplicatesIpv6(){
        try {
            // 去重（将采集到的数据，复制并创创建相同结构的表中，并去除重复数据（mac + ip），使用存储过程完成）
            this.ipv6Service.removeDuplicates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取设备端口
     */
    @Override
    public void gatherPort(Date date, List<NetworkElement> networkElements){

        if(networkElements.size() <= 0){
            networkElements = this.getGatherDevice();
        }

        if(networkElements.size() > 0) {

            this.portService.copyGatherData();

            this.portService.truncateTableGather();

            CountDownLatch latch = new CountDownLatch(networkElements.size());

            for (NetworkElement networkElement : networkElements) {


                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    continue;
                }


                gatherDataThreadPool.execute(new GatherPortSNMPRunnable(networkElement, date, latch));

            }

            try {

                boolean completed = latch.await(10, TimeUnit.MINUTES);
                log.info("采集结果：{}", completed ? "COMPLETED" : "TIMEOUT");

                log.info("处理完成，线程池状态: {}", gatherDataThreadPool.getPoolStatus());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("处理被中断");
            }
        }
    }

    @Override
    public void gatherPortIpv6(Date date, List<NetworkElement> networkElements){
        if(networkElements.size() <= 0){
            networkElements = this.getGatherDevice();
        }

        if(networkElements.size() > 0) {

            this.portIpv6Service.copyGatherData();

            this.portIpv6Service.truncateTableGather();

            CountDownLatch latch = new CountDownLatch(networkElements.size());

            for (NetworkElement networkElement : networkElements) {
                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    continue;
                }
//                GatherDataThreadPool.getInstance().addThread(new GatherPortIpv6Runnable(networkElement, date, latch));
                gatherDataThreadPool.execute(new GatherPortIpv6SNMPRunnable(networkElement, date, latch));

            }
            try {

                boolean completed = latch.await(10, TimeUnit.MINUTES);
                log.info("采集结果：{}", completed ? "COMPLETED" : "TIMEOUT");

                log.info("处理完成，线程池状态: {}", gatherDataThreadPool.getPoolStatus());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("处理被中断");
            }
        }
    }

    @Override
    public void gatherIsIpv6(Date date) {

        List<NetworkElement> networkElements = this.getGatherDevice();

        CountDownLatch latch = new CountDownLatch(networkElements.size());

        if(networkElements.size() > 0) {

            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    continue;
                }

                gatherDataThreadPool.execute(new GatherIsIpv6SNMPRunnable(networkElement, date, latch));
            }

            try {

                boolean completed = latch.await(10, TimeUnit.MINUTES);
                log.info("采集结果：{}", completed ? "COMPLETED" : "TIMEOUT");

                log.info("处理完成，线程池状态: {}", gatherDataThreadPool.getPoolStatus());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("处理被中断");
            }
        }
    }

    @Override
    public void gatherFlux(Date date) {
        log.info("flux runing...");
        List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(null);

        if (fluxConfigs.size() > 0) {

            BigDecimal ipv4Sum = new BigDecimal(0);
            BigDecimal ipv6Sum = new BigDecimal(0);
            BigDecimal ipv6Rate = new BigDecimal(0);


            // 获取全部ipv4流量
            List<Map<String, String>> v4_list = new ArrayList<>();
            List<Map<String, String>> v6_list = new ArrayList<>();
            for (FluxConfig fluxConfig : fluxConfigs) {

                // 获取ipv4流量
                // 1. 遍历oid
                if(fluxConfig.getIpv4Oid() != null && !"".equals(fluxConfig.getIpv4Oid())){
                    List<List<String>> v4_oids = JSONObject.parseObject(fluxConfig.getIpv4Oid(), List.class);
                    for (List<String> oid : v4_oids) {

                        if(oid.size() > 0 && oid.size() >= 2){
                            String in = String.valueOf(oid.get(0));
                            String out = String.valueOf(oid.get(1));
                            if(in.equals("") || out.equals("")){
                                continue;
                            }
                            String result = gettraffic(fluxConfig.getIpv4(), fluxConfig.getVersion(), fluxConfig.getCommunity(), in, out);
                            if (StringUtils.isNotEmpty(result)) {
                                Map map = JSONObject.parseObject(result, Map.class);
                                v4_list.add(map);
                            }
                        }
                    }
                }

                if(fluxConfig.getIpv6Oid() != null && !"".equals(fluxConfig.getIpv6Oid())){
                    List<List<String>> v6_oids = JSONObject.parseObject(fluxConfig.getIpv6Oid(), List.class);
                    for (List<String> oid : v6_oids) {

                        if(oid.size() > 0 && oid.size() >= 2) {
                            String in = String.valueOf(oid.get(0));
                            String out = String.valueOf(oid.get(1));
                            if(in.equals("") || out.equals("")){
                                continue;
                            }
                            String result = gettraffic(fluxConfig.getIpv6(), fluxConfig.getVersion(), fluxConfig.getCommunity(), in, out);
                            if (StringUtils.isNotEmpty(result)) {
                                Map map = JSONObject.parseObject(result, Map.class);
                                v6_list.add(map);
                            }
                        }
                    }
                }
            }

            if (v4_list.size() > 0) {

                BigDecimal in = v4_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal out = v4_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("out")))).reduce(BigDecimal.ZERO, BigDecimal::add);
                ipv4Sum = in.add(out);
            }

            if (v6_list.size() > 0) {


                BigDecimal in = v6_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal out = v6_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("out")))).reduce(BigDecimal.ZERO, BigDecimal::add);
                ipv6Sum = in.add(out);
            }


            FlowStatistics flowStatistics = new FlowStatistics();
            flowStatistics.setAddTime(date);
            flowStatistics.setIpv4Sum(ipv4Sum);
            flowStatistics.setIpv6Sum(ipv6Sum);
            Map params = new HashMap();
            params.clear();
            params.put("oneMinuteAgo", getLastMinute());
            List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
            // 判断是否为连续采集
//            Date lastMinute = DateTools.getMinDate(-1);
//            params.clear();
//            params.put("time", lastMinute);
//            List<FlowStatistics> lastFLowStatistics = this.flowStatisticsService.selectObjByMap(params);
//            if(lastFLowStatistics.size() == 0){
//                this.flowStatisticsService.save(flowStatistics);
//                return;
//            }
            if(flowStatisticsList.size() > 0){
                Date lastMinute = DateTools.getMinDate(-1, date);
                FlowStatistics obj = flowStatisticsList.get(0);
                if(obj.getAddTime().getTime() != lastMinute.getTime()){
                    this.flowStatisticsService.save(flowStatistics);
                    return;
                }
            }

//            params.clear();
//            params.put("oneMinuteAgo", getLastMinute());
//            List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);

            // 判断flux配置是否更新
            for (FluxConfig fc : fluxConfigs) {
                if(fc.getUpdate() == 1){
                    fc.setUpdate(0);
                    this.fluxConfigService.update(fc);
                    this.flowStatisticsService.save(flowStatistics);
                    return;
                }
            }

            if(ipv4Sum.equals(BigDecimal.ZERO) && ipv6Sum.equals(BigDecimal.ZERO)){

                flowStatistics.setIpv4Sum(new BigDecimal(0));
                flowStatistics.setIpv6Sum(new BigDecimal(0));
                this.flowStatisticsService.save(flowStatistics);
                return;
            }

            // 计算
            if(flowStatisticsList.size() > 0){

                FlowStatistics obj = flowStatisticsList.get(0);
                if(obj.getIpv4Sum() != null && obj.getIpv6Sum() != null
                        && !obj.getIpv4Sum().equals(BigDecimal.ZERO) && !obj.getIpv6Sum().equals(BigDecimal.ZERO)
                        && !ipv4Sum.equals(BigDecimal.ZERO) && !ipv4Sum.equals(BigDecimal.ZERO)){


                    ipv4Sum = ipv4Sum.subtract(obj.getIpv4Sum());
                    ipv6Sum = ipv6Sum.subtract(obj.getIpv6Sum());
                    BigDecimal in = ipv4Sum.multiply(new BigDecimal(8)).divide(new BigDecimal(60*1024*1024), 2,BigDecimal.ROUND_HALF_UP);
                    BigDecimal out = ipv6Sum.multiply(new BigDecimal(8)).divide(new BigDecimal(60*1024*1024), 2,BigDecimal.ROUND_HALF_UP);
                    flowStatistics.setIpv4(in);
                    flowStatistics.setIpv6(out);
                    if(!ipv4Sum.equals(BigDecimal.ZERO) || !ipv6Sum.equals(BigDecimal.ZERO)){
                        // ipv6流量占比=ipv6流量/（ipv4流量+ipv6流量）
                        ipv6Rate = out.divide(in.add(out), 2,BigDecimal.ROUND_HALF_UP);
                    }
                    flowStatistics.setIpv6Rate(ipv6Rate);
                }
            }


            this.flowStatisticsService.save(flowStatistics);
        }
    }

    @Override
    public void exec(Date date) {
        pingService.exec();
    }

    @Override
    public void pingSubnet() {
        this.subnetService.pingSubnet();
    }

    @Override
    public void gatherSnmpStatus() {

        Set<String> keys = new HashSet<>();

        List<NetworkElement> nes = this.networkElementService.selectObjAllByGather();

        if(nes.size() > 0){
            for (NetworkElement element : nes) {

                String hostName = getHostName(element);
                if(StringUtils.isNotEmpty(hostName)){
                    String key = element.getUuid();
                    keys.add(key);
                }
            }
            // 更新redis
            this.snmpStatusUtils.editSnmpStatus(keys);
        }
    }

    // 获取设备名
    public String getHostName(NetworkElement element){

        String hostName = "";

        String path = Global.PYPATH + "gethostname.py";
        String[] args = {element.getIp(), element.getVersion(),
                element.getCommunity()};
        hostName = pythonExecUtils.exec(path, args);

//        SNMPParams snmpParams = new SNMPParams(element.getIp(), element.getVersion(), element.getCommunity());
//        hostName = SNMPv2Request.getDeviceName(snmpParams);  // 获取设备名

        return hostName;
    }

    // 获取上一分钟的时间
    public static Date getLastMinute(){

        Calendar cal = Calendar.getInstance();

        // 获取当前时间
        long currentTime = cal.getTimeInMillis();

        // 将当前时间减去一分钟
        cal.setTimeInMillis(currentTime - 60000);

        // 获取减去一分钟后的时间
        Date oneMinuteAgo = cal.getTime();
        return oneMinuteAgo;
    }

    public String gettraffic(String ip, String version, String community, String in, String out) {
//        String path = Global.PYPATH + "gettraffic.py";
//        String[] params = {ip, "v2c",
//                community, in, out};
//
//        String result = pythonExecUtils.exec2(path, params);
//        if(StringUtil.isNotEmpty(result)){
//            return result;
//        }
//        return null;

        version = "v2c";

        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version(version)
                .host(ip)
                .version(version)
                .community(community)
                .build();

        String traffic = SNMPv3Request.getTraffic(snmpv3Params, in, out);
        if(StringUtil.isNotEmpty(traffic)){
            return traffic;
        }
        return "";
    }


    public static void main(String[] args) {
//        String path = Global.PYPATH + "gettraffic.py";
//        String[] params = {ip, "v2c",
//                community, in, out};
//
//        String result = pythonExecUtils.exec2(path, params);
//        if(StringUtil.isNotEmpty(result)){
//            return result;
//        }
//        return null;

//            SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
//                    .host("113.240.243.196")
//                    .version("v2c")
//                    .community("transfar@123")
//                    .build();
//
//            String traffic = SNMPv3Request.getTraffic(snmpv3Params, "1.3.6.1.2.1.31.1.1.1.6.6", "1.3.6.1.2.1.31.1.1.1.10.6");
//            log.info("流量：{}", traffic);

        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .host("113.240.243.196")
                .version("v2c")
                .community("transfar@123")
                .build();
        String traffic = SNMPv3Request.getDeviceName(snmpv3Params);

        log.info("主机名：{}", traffic);

    }

}
