package com.metoo.nrsm.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.GatherFactory;
import com.metoo.nrsm.core.mapper.ProbeMapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.enums.LogStatusType;
import com.metoo.nrsm.core.vo.ProbeRequestVO;
import com.metoo.nrsm.entity.Probe;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.ProbeResult;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:31
 */
@Slf4j
@Service
@Transactional
public class ProbeServiceImpl implements IProbeService {

    @Resource
    private ProbeMapper probeMapper;
    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private ApiService apiService;
    @Autowired
    private IProbeResultService probeResultService;
    @Autowired
    private IArpService arpService;
    @Autowired
    private ISurveyingLogService surveyingLogService;


    @Override
    public List<Probe> selectObjByMap(Map params) {
        List<Probe> probes = this.probeMapper.selectObjByMap(params);
        return probes;
    }

    @Override
    public List<Probe> selectProbeBackByMap(Map params) {
        return probeMapper.selectProbeBackByMap(params);
    }

    @Override
    public List<Probe> mergeProbesByIp() {
        List<Probe> probes = this.probeMapper.mergeProbesByIp();
        return probes;
    }

    @Override
    public List<Probe> selectDeduplicationByIp(Map params) {
        List<Probe> probes = this.probeMapper.selectObjByMap(params);
        return mergeProbes(probes);
    }

    public static List<Probe> mergeProbes(List<Probe> probes) {
        // Create a map to store the merged probes by ip_addr and ipv6
        Map<String, Probe> mergedProbes = new HashMap<>();

        for (Probe probe : probes) {
            // 合并v4和仅有v6 probe
            if(StringUtil.isNotEmpty(probe.getIp_addr())){
                mergeByIp(probe.getIp_addr(), probe, mergedProbes);
            }else{
                mergeByIp(probe.getIpv6(), probe, mergedProbes);
            }
        }

        // Return merged results as a list
        return new ArrayList<>(mergedProbes.values());
    }

    private static void mergeByIp(String ip, Probe probe, Map<String, Probe> mergedProbes) {
        if (StringUtil.isNotEmpty(ip)) {
            if (!mergedProbes.containsKey(ip)) {
                // Initialize TTLs to avoid null
                probe.setTtls(getTtlAsString(probe.getTtl()));
                mergedProbes.put(ip, probe);  // Add first occurrence
            } else {
                Probe existing = mergedProbes.get(ip);
                // Merge fields: vendor, os_gen, os_family, application_protocol, ttl
                existing.setVendor(mergeField(existing.getVendor(), probe.getVendor()));
                existing.setOs_gen(mergeField(existing.getOs_gen(), probe.getOs_gen()));
                existing.setOs_family(mergeField(existing.getOs_family(), probe.getOs_family()));
                existing.setApplication_protocol(mergeField(existing.getApplication_protocol(), probe.getApplication_protocol()));
                existing.setTtls(mergeField(getTtlAsString(existing.getTtl()), getTtlAsString(probe.getTtl())));
                existing.setPort_service_vendor(mergeField(existing.getPort_service_vendor(), probe.getPort_service_vendor()));
            }
        }
    }

    private static String getTtlAsString(Integer ttl) {
        return Optional.ofNullable(ttl).map(String::valueOf).orElse("");
    }

    private static String mergeField(String existingValue, String newValue) {
        if (existingValue == null || existingValue.isEmpty()) {
            if (newValue == null || newValue.isEmpty()) {
                return null;
            }
            return newValue;
        }
        if (newValue == null || newValue.isEmpty()) {
            return existingValue;
        }
        return existingValue + "," + newValue;
    }

    @Override
    public boolean insert(Probe instance) {
        try {
            instance.setCreateTime(new Date());
            this.probeMapper.insert(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Probe instance) {
        try {
            this.probeMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int delete(Integer id) {
        try {
            return this.probeMapper.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int deleteTable() {
        try {
            return this.probeMapper.deleteTable();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int deleteTableBack() {
        return this.probeMapper.deleteTableBack();
    }

    @Override
    public int copyToBck() {
        return this.probeMapper.copyToBck();
    }

    @Override
    public boolean deleteProbeByIp(String ipv4, String ipv6) {
        try {
            // 使用stream API和组合查询条件
            Map<String, Object> params = new HashMap<>();
            if (StringUtil.isNotEmpty(ipv4)) {
                params.put("ip_addr", ipv4);
            }
            if (StringUtil.isNotEmpty(ipv6)) {
                params.put("ipv6", ipv6);
            }

            List<Probe> deleteProbes = probeMapper.selectObjByMap(params);

            if (!deleteProbes.isEmpty()) {
                deleteProbes.forEach(probe -> {
                    try {
                        probeMapper.delete(probe.getId());
                    } catch (Exception e) {
                        e.printStackTrace();  // 可根据需要优化异常处理
                    }
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

//    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void scanByTerminal() {

        String begin_time = DateTools.getCreateTime();
        int probeLogId = surveyingLogService.createSureyingLog("全网资产扫描", begin_time, 1, null, 3);

        // 1.查询全部终端（采集表）
//        Map params = new HashMap();
//        params.put("v4ipIsNull", "v4ipIsNull");
//        List<Terminal> terminals = this.terminalService.selectObjByMap(params);
//        if(!terminals.isEmpty()){
            // 调用创发接口
            try {
                getProbeResult();
            } catch (Exception e) {
                e.printStackTrace();
                surveyingLogService.updateSureyingLog(probeLogId, LogStatusType.FAIL.getCode());
            }
//        }
        surveyingLogService.updateSureyingLog(probeLogId, LogStatusType.SUCCESS.getCode());
    }

//    public void getProbeResult() {
//
//        this.probeMapper.deleteTable();
//
//        Map params = new HashMap();
//        params.put("online", true);
//        List<Terminal> terminals = this.terminalService.selectObjByMap(params);
//        if(terminals.size() > 0){
//            if (terminals.size() >= 300) {
//                // 拆分v4|v6
//                List<Terminal> ipv4List = this.terminalService.selectObjToProbe(MapUtil.of("ipv4IsNotNull", true));
//                if (ipv4List.size() >= 300) {
//                    processInBatches(ipv4List);
//                } else {
//                    processSingleBatch(ipv4List);
//                }
//                List<Terminal> ipv6List = this.terminalService.selectObjToProbe(MapUtil.of("ipv6IsNotNull", true));
//                if (ipv6List.size() >= 300) {
//                    processInBatches(ipv6List);
//                } else {
//                    processSingleBatch(ipv6List);
//                }
//            } else {
//                List<Terminal> ipv4List = this.terminalService.selectObjToProbe(MapUtil.of("ipv4IsNotNull", true));
//                if (!ipv4List.isEmpty()) {
//                    processSingleBatch(ipv4List);
//                }
//                List<Terminal> ipv6List = this.terminalService.selectObjToProbe(MapUtil.of("ipv6IsNotNull", true));
//                if (!ipv6List.isEmpty()) {
//                    processSingleBatch(ipv6List);
//                }
//            }
//        }
//
//        // 补充针对表中剩余的条目再放入probe表中，端口写2，再进行os-scanner扫描（删除条目）
//        // 去重 probe
//        List<String> ips = this.probeMapper.selectObjDistinctByIp();
//        params.clear();
//        params.put("notInIps", ips);
//        List<Terminal> terminalList = terminalService.selectObjByMap(params);
//        if (CollUtil.isNotEmpty(terminalList)) {
//            for (Terminal terminal : terminalList) {
//                Probe probe = Convert.convert(Probe.class, terminal);
//                probe.setIp_addr(terminal.getV4ip());
//                probe.setIpv6(terminal.getV6ip());
//                probe.setPort_num("2");
//                probe.setMac(terminal.getMac());
//                probe.setMac_vendor(terminal.getMacVendor());
//                List<Probe> probeList = null;
//
//                if (StringUtil.isNotEmpty(probe.getIp_addr())) {
//                    probeList = this.selectObjByMap(MapUtil.of("ip_addr", terminal.getV4ip()));
//                } else {
//                    probeList = this.selectObjByMap(MapUtil.of("ipv6", probe.getIpv6()));
//                }
//                if (CollUtil.isEmpty(probeList)) {
//                    // 不存在，则插入到probe表
//                    this.insert(probe);
//                    // 删除
////                            arpService.delete(arp.getId());
//                }
//            }
//        }
//
//        GatherFactory factory = new GatherFactory();
//        Gather gather = factory.getGather("fileToProbe");
//        gather.executeMethod();
//
//        this.deleteTableBack();
//
//        this.copyToBck();
//
//        this.writeTerminal();
//    }


    public void getProbeResult() {

        this.probeMapper.deleteTable();

        Map params = new HashMap();
        params.put("online", true);
        List<Terminal> terminals = this.terminalService.selectObjByMap(params);
        if(terminals.size() > 0){
            if (terminals.size() >= 300) {
                // 拆分v4|v6
                List<Terminal> ipv4List = this.terminalService.selectObjToProbe(MapUtil.of("ipv4IsNotNull", true));
                if (ipv4List.size() >= 300) {
                    processInBatches(ipv4List);
                } else {
                    processSingleBatch(ipv4List);
                }
                List<Terminal> ipv6List = this.terminalService.selectObjToProbe(MapUtil.of("ipv6IsNotNull", true));
                if (ipv6List.size() >= 300) {
                    processInBatches(ipv6List);
                } else {
                    processSingleBatch(ipv6List);
                }
            } else {
                List<Terminal> ipv4List = this.terminalService.selectObjToProbe(MapUtil.of("ipv4IsNotNull", true));
                if (!ipv4List.isEmpty()) {
                    processSingleBatch(ipv4List);
                }
                List<Terminal> ipv6List = this.terminalService.selectObjToProbe(MapUtil.of("ipv6IsNotNull", true));
                if (!ipv6List.isEmpty()) {
                    processSingleBatch(ipv6List);
                }
            }
        }

        // 补充针对表中剩余的条目再放入probe表中，端口写2，再进行os-scanner扫描（删除条目）
        // 去重 probe
        List<String> ips = this.probeMapper.selectObjDistinctByIp();
        params.clear();
        params.put("notInIps", ips);
        List<Terminal> terminalList = terminalService.selectObjByMap(params);
        if (CollUtil.isNotEmpty(terminalList)) {
            for (Terminal terminal : terminalList) {
                Probe probe = Convert.convert(Probe.class, terminal);
                probe.setIp_addr(terminal.getV4ip());
                probe.setIpv6(terminal.getV6ip());
                probe.setPort_num("2");
                probe.setMac(terminal.getMac());
                probe.setMac_vendor(terminal.getMacVendor());
                List<Probe> probeList = null;

                if (StringUtil.isNotEmpty(probe.getIp_addr())) {
                    probeList = this.selectObjByMap(MapUtil.of("ip_addr", terminal.getV4ip()));
                } else {
                    probeList = this.selectObjByMap(MapUtil.of("ipv6", probe.getIpv6()));
                }
                if (CollUtil.isEmpty(probeList)) {
                    // 不存在，则插入到probe表
                    this.insert(probe);
                    // 删除
//                            arpService.delete(arp.getId());
                }
            }
        }


        // 查询metoo_probe_bck，查看是否因动态分配ip地址，导致mac与ip地址对应错误条目，修改metoo_probe_bck mac地址，
        syncProbeIpWithTerminal();
        // 增量添加probe;
        findDiffBetweenProbeAndBackup();


        // 修改逻辑，扫描在后，增量更新
        GatherFactory factory = new GatherFactory();
        Gather gather = factory.getGather("fileToProbe");
        gather.executeMethod();

        this.deleteTableBack();
        this.copyToBck();

        this.writeTerminal();
    }

    // 查询metoo_probe_bck，查看是否因动态分配ip地址，导致mac与ip地址对应错误条目，修改metoo_probe_bck mac地址，
    public void syncProbeIpWithTerminal(){
        int backupProbe = this.probeMapper.syncProbeIpWithTerminal();
        log.info("更新动态ip条数{}", backupProbe);
    }

    public void findDiffBetweenProbeAndBackup(){
        int backupProbe = this.probeMapper.syncProbeDiffToBackup();
        log.info("备份新增探针数据{}", backupProbe);
    }

    private boolean processInBatches(List<Terminal> arpList) {
        int batchSize = 100;
        boolean flag = true;
        for (int i = 0; i < arpList.size(); i += batchSize) {
            List<Terminal> subList = arpList.subList(i, Math.min(arpList.size(), i + batchSize));
            // ipv4 调用创发接口
            String ipAddressString = extractIpAddresses(subList);
            if (StringUtil.isNotEmpty(ipAddressString)) {
                flag = callChuangfa(ipAddressString);
            }
            if (!flag) {
                return flag;
            }
            // ipv6调用创发接口
            String ipAddressStringIpv6 = extractIpAddressesByIpv6(subList);
            if (StringUtil.isNotEmpty(ipAddressStringIpv6)) {
                flag = callChuangfa(ipAddressStringIpv6);
            }
            if (!flag) {
                return flag;
            }

        }
        return true;
//        executeGatherAndBackup();
    }

    private void processSingleBatch(List<Terminal> arpList) {
        boolean ipv4Flag;
        boolean ipv6Flag;
        String ipAddressString = extractIpAddresses(arpList);
        String ipAddressStringIpv6 = extractIpAddressesByIpv6(arpList);
        if (StringUtil.isNotEmpty(ipAddressString)) {
            ipv4Flag = this.callChuangfaSingle(ipAddressString);
        } else {
            ipv4Flag = true;
        }
        if (StringUtil.isNotEmpty(ipAddressStringIpv6)) {
            ipv6Flag = this.callChuangfaSingle(ipAddressStringIpv6);
        } else {
            ipv6Flag = true;
        }
    }


    @Value("${AP.URL}")
    private String apUrl;

    public boolean callChuangfaSingle(String ipAddresses) {
        log.info("Ipaddress================" + ipAddresses);
        try {
            ProbeRequestVO jsonRequest = new ProbeRequestVO();

            jsonRequest.setTaskuuid(UUID.randomUUID().toString());

            jsonRequest.setThread("600");

            jsonRequest.setTimeout("300");

            jsonRequest.setIp(ipAddresses);

            String result = apiService.callThirdPartyApi(apUrl, jsonRequest);

            if (result != null) {
                ProbeResult probeResult = this.probeResultService.selectObjByOne();
                try {
                    this.probeWaitSingle(result, probeResult);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean callChuangfa(String ipAddresses) {
        log.info("Ipaddress================" + ipAddresses);
        try {
            ProbeRequestVO jsonRequest = new ProbeRequestVO();

            jsonRequest.setTaskuuid(UUID.randomUUID().toString());

            jsonRequest.setThread("600");

            jsonRequest.setTimeout("300");

            jsonRequest.setIp(ipAddresses);

            String result = apiService.callThirdPartyApi(apUrl, jsonRequest);

            if (result != null) {
                ProbeResult probeResult = this.probeResultService.selectObjByOne();
                try {
                    this.probeWaitSingle(result, probeResult);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void probeWaitSingle(String result, ProbeResult probeResult) {

        JSONObject json = JSONObject.parseObject(result);

        if (json.getInteger("code") == 0) {
            // 等待
            Map params = new HashMap();
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                params.clear();
                params.put("result", probeResult.getResult() + 1);
                List<ProbeResult> obj = this.probeResultService.selectObjByMap(params);
                if (obj.size() > 0) {
                    break;
                }
            }
        }
    }

    private String extractIpAddresses(List<Terminal> list) {
        String ipStr = list.stream().filter(temp -> StrUtil.isNotEmpty(temp.getV4ip()))
                .map(Terminal::getV4ip)
                .collect(Collectors.joining(","));
        return ipStr;
    }

    private String extractIpAddressesByIpv6(List<Terminal> arpList) {
        String ipv6Str = arpList.stream().filter(temp -> StrUtil.isEmpty(temp.getV4ip()) && StrUtil.isNotEmpty(temp.getV6ip()))
                .map(Terminal::getV6ip)
                .collect(Collectors.joining(","));
        return ipv6Str;
    }

    // 写回终端表 合并vendor,os_gen,os_family
    // 判断ttl写os
    public void writeTerminal(){
        Map params = new HashMap();
        params.put("online", true);
        List<Terminal> terminals = this.terminalService.selectObjByMap(params);
        List<Probe> probes = this.mergeProbesByIp();
        if(probes.isEmpty() || terminals.isEmpty()){
            return;
        }
        Map<String, Probe> map = new HashMap<>();
        for (Probe probe : probes) {
            map.put(probe.getIp_addr(), probe);
        }
        outerLoop: // 给外层循环加个标签
        for (Terminal terminal : terminals) {
            Probe probe = map.get(terminal.getV4ip());
            if(probe != null){
                boolean device = false;
                List list = new ArrayList();
                String combined = probe.getCombined();
                String[] combineds = combined.split(",");
                if(combineds.length > 0){
                    for (String ele : combineds) {
                        Map stats = new HashMap();
                        String[] eles = ele.split("/", 3);// 字符串的末尾或连续分隔符之间可能会包括一个分隔符本身
                        if(eles.length > 0){
                            String port_num = eles[0];
                            if(port_num.equals("2")){
//                                continue outerLoop; // 使用标签跳出外层循环
                                continue;
                            }
                            String application_protocol = eles[1];
                            if(application_protocol.contains("telnet")){
                                device = true;
                                continue outerLoop; // 使用标签跳出外层循环
                            }
                            String title = eles[2];
                            stats.put("port_num", port_num);
                            stats.put("application_protocol", application_protocol);
                            stats.put("title", title);
                            list.add(stats);
                        }
                    }
                }
                boolean flag = false;

                String combined_os = probe.getCombined_os();
                String combined_ttl = probe.getCombined_ttl();
                if(StringUtils.isNotBlank(combined_ttl)){
                    String[] ttls = combined_ttl.split(",");
                    if(ttls.length > 0){
                        for (String ttl : ttls) {
                            if(Integer.parseInt(ttl) > 120 && Integer.parseInt(ttl) < 129){
                                if(StringUtil.isEmpty(combined_os)){
                                    combined_os = "Windows";
                                    flag = true;
                                    break;
                                }
                            }else if(Integer.parseInt(ttl) > 200){
                                flag = true;
                                device = true;
//                                combined_os = "device";
                                break;
                            }
                        }
                    }
                }
                List<JSONObject> osList = new ArrayList();
                if(StringUtils.isNotEmpty(combined_os)){
                    if(!flag){
                        osList = parseInputToJsonList(combined_os);
                    }else{
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("vendor", combined_os);
                        osList.add(jsonObject);
                    }
                }
                if(osList.size() > 0){
                    JSONObject jsonObject = osList.get(0);
                    terminal.setOs(jsonObject.getString("vendor"));

                }

                if(probe.getCombined_vendor().toLowerCase().contains("Ruijie".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("Tenda".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("h3c".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("TP-LINK".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("mercury".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("Device".toLowerCase())){
                    device = true;
                }
                if(probe.getCombined_application_protocol().toLowerCase().contains("telnet".toLowerCase())){
                    device = true;
                }

                terminal.setCombined_vendor_gen_family(JSONObject.toJSONString(osList));
                terminal.setCombined_port_protocol(JSONObject.toJSONString(list));
                if(device){
                    terminal.setDeviceType(1);
                }
                this.terminalService.update(terminal);
            }
        }


        // 计算nswitch
        // 查询所有nswitch，根据名称分组查询，查找deviceType为1的terminal条目，将改条目ip地址，填入到其他不为1的temrinal条目的deviceIp中

        // 更新nswitch终端设备ip
        List<Terminal> terminalList = this.terminalService.selectDeviceIpByNSwitch();
        if(!terminalList.isEmpty()){
            for (Terminal terminal : terminalList) {
                this.terminalService.update(terminal);
            }
        }
    }

    public static List<JSONObject> parseInputToJsonList(String input) {
        // 存储解析后的 JSON 对象列表
        List<JSONObject> jsonList = new ArrayList<>();

        String os = input.replaceAll(":", "").replaceAll(",","");
        if("".equals(os)){
            return jsonList;
        }

        if((input == null || !input.isEmpty()) && !input.contains(":")){
            return jsonList;
        }

        // 分割输入字符串，基于逗号分割多个数据项
        String[] items = input.split(",");

        // 遍历每个数据项
        for (String item : items) {
            // 按冒号分割
            String[] parts = item.split(":", 3);

            // 确保每个项包含三部分
            if (parts.length == 3) {
                String vendor = parts[0];
                String osGen = parts[1];
                String osFamily = parts[2];
                if(!StringUtils.isEmpty(vendor)
                        || !StringUtils.isEmpty(osGen) || !StringUtils.isEmpty(osFamily)){
                    // 创建 JSON 对象并存储数据
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("vendor", vendor);
                    jsonObject.put("os_gen", osGen);
                    jsonObject.put("os_family", osFamily);
                    // 将 JSON 对象添加到列表中
                    jsonList.add(jsonObject);
                }
            }
        }

        return jsonList;
    }


    public static void main(String[] args) {

//        // 1
//        String a = "2/adtran//total_access_904/";
//        String[] aa = a.split("/", 5);
//
//        // 输出第五个元素
//        System.out.println(aa[4]);
//

        // 2
        String input = "::"; // 输入数据

        List jsonList = parseInputToJsonList(input);
        // 输出结果
        System.out.println(jsonList);
    }


}
