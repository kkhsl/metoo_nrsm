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
import com.metoo.nrsm.core.service.IArpService;
import com.metoo.nrsm.core.service.IProbeResultService;
import com.metoo.nrsm.core.service.IProbeService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.core.vo.ProbeRequestVO;
import com.metoo.nrsm.entity.Probe;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.ProbeResult;
import lombok.extern.slf4j.Slf4j;
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


    @Override
    public List<Probe> selectObjByMap(Map params) {
        List<Probe> probes = this.probeMapper.selectObjByMap(params);
        return probes;
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

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void scanByTerminal() {
        // 1.查询全部终端（采集表）
        List<Terminal> terminals = this.terminalService.selectObjByMap(Collections.EMPTY_MAP);
        if(!terminals.isEmpty()){
            // 调用创发接口
            getProbeResult();
        }
    }


    public String getProbeResult() {

        this.probeMapper.deleteTable();

        List<Terminal> terminals = this.terminalService.selectObjToProbe(Collections.EMPTY_MAP);

        try {

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
                processSingleBatch(terminals);
            }

            try {

                //补充针对arp表中剩余的条目再放入probe表中，端口写2，再进行os-scanner扫描（删除条目）
                List<Terminal> terminalList = terminalService.selectObjByMap(null);
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

                log.info("================================ os-scanner ========================");
                GatherFactory factory = new GatherFactory();
                Gather gather = factory.getGather("fileToProbe");
                gather.executeMethod();



                this.deleteTableBack();

                this.copyToBck();

                this.writeTerminal();

            } catch (Exception e) {
                e.printStackTrace();
            }



            log.info("Probe end===============");

            return null;
        } catch (Exception e) {
        }
        log.info("Probe end===============");
        return null;
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

    public static void main(String[] args) {
        String a = "2/adtran//total_access_904/";
        String[] aa = a.split("/", 5);

        // 输出第五个元素
        System.out.println(aa[4]);
    }


    // 写回终端表 合并vendor,os_gen,os_family
    // 判断ttl写os
    public void writeTerminal(){
        List<Terminal> terminals = this.terminalService.selectObjByMap(Collections.EMPTY_MAP);
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

                List list = new ArrayList();
                String combined = probe.getCombined();
                String[] combineds = combined.split(",");
                if(combineds.length > 0){
                    for (String ele : combineds) {
                        Map stats = new HashMap();
                        String[] eles = ele.split("/", 2);// 字符串的末尾或连续分隔符之间可能会包括一个分隔符本身
                        if(eles.length > 0){
                            String port_num = eles[0];
                            if(port_num.equals("2")){
                                continue outerLoop; // 使用标签跳出外层循环
                            }
                            String application_protocol = eles[1];
                            stats.put("port_num", port_num);
                            stats.put("application_protocol", application_protocol);
                            list.add(stats);
                        }
                    }
                }
                String os = "";
                String combined_os = probe.getCombined_os();
                boolean flag = false;
                String combined_ttl = probe.getCombined_ttl();
                if(StringUtils.isNotBlank(combined_ttl)){
                    String[] ttls = combined_ttl.split(",");
                    if(ttls.length > 0){
                        for (String ttl : ttls) {
                            if(Integer.parseInt(ttl) > 120 && Integer.parseInt(ttl) < 129){
                                if(StringUtil.isEmpty(combined_os)){
                                    os = "Windows";
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                String vendor = probe.getCombined_vendor();
                if(!flag && vendor != null && (
                        vendor.toLowerCase().contains("microsoft")
                                || vendor.toLowerCase().contains("apple")
                                || vendor.toLowerCase().contains("google"))){
                    os = combined_os;
                    flag = true;
                }

                String application_protocol = probe.getCombined_application_protocol();

                if(!flag && application_protocol != null && (application_protocol.toLowerCase().contains("msrpc")
                        || application_protocol.toLowerCase().contains("netbios-ssn")
                        || application_protocol.toLowerCase().contains("ms-wbt-server")
                        || application_protocol.toLowerCase().contains("microsoft-ds"))){
                    os = "Windows";
                }


                terminal.setOs(os);
                terminal.setCombined(JSONObject.toJSONString(list));
                this.terminalService.update(terminal);
            }
        }
    }
}