package com.metoo.nrsm.core.utils.gather.snmp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPParamFactory;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.IMacService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class MacManager {

    public void getMac(NetworkElement networkElement, Date date){
        String hostName = getHostName(networkElement);
        if(!StringUtils.isEmpty(hostName)){
            processNetworkElementData(networkElement, hostName, date);
        }
    }

    private String getHostName(NetworkElement networkElement){
//        String hostName = SNMPv2Request.getDeviceName(new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity()));
        String hostName = SNMPv3Request.getDeviceName(SNMPParamFactory.createSNMPParam(networkElement));
        return hostName;
    }

    // 处理网络元素数据
    private void processNetworkElementData(NetworkElement networkElement, String hostName, Date date) {
//        getLldpDataSNMP(networkElement, date, hostName);

        getMacData(networkElement, date, hostName);

        getPortMacData(networkElement, date, hostName);
    }

    public void getLldpDataSNMP(NetworkElement networkElement, Date date, String hostName) {
        try {
//            SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
//            org.json.JSONArray result = SNMPv2Request.getLldp(snmpParams);
            JSONArray result = SNMPv3Request.getLldp(SNMPParamFactory.createSNMPParam(networkElement));
            if (!result.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map> lldps = objectMapper.readValue(result.toString(), new TypeReference<List<Map>>(){});
                this.setRemoteDevice(networkElement, lldps, hostName, date);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    public void getMac2(NetworkElement networkElement, Date date){
        String hostName = getHostName(networkElement);
        if(!StringUtils.isEmpty(hostName)){
            processNetworkElementDataFuture(networkElement, hostName, date);
        }
    }
    // 处理网络元素数据
    private void processNetworkElementDataFuture(NetworkElement networkElement, String hostName, Date date) {
        log.info("Processing data for network element: {}", networkElement.getIp());

        // 使用 CompletableFuture 进行并行处理
        CompletableFuture<Void> lldpFuture = CompletableFuture.runAsync(() -> {
            getLldpDataSNMP(networkElement, date, hostName);
        });

        CompletableFuture<Void> macFuture = CompletableFuture.runAsync(() -> {
            getMacData(networkElement, date, hostName);
        });

        CompletableFuture<Void> portMacFuture = CompletableFuture.runAsync(() -> {
            getPortMacData(networkElement, date, hostName);
        });

        // 等待所有任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(lldpFuture, macFuture, portMacFuture);

        try {
            allOf.join(); // 等待所有子任务完成
        } catch (Exception e) {
            log.error("Error processing SNMP data for IP: {}", networkElement.getIp(), e);
        }

        log.info("Finished processing data for network element: {}", networkElement.getIp());
    }

    public void getMacData(NetworkElement networkElement, Date date, String hostName){
        try {
            SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
//            JSONArray result = SNMPv2Request.getMac(snmpParams);
            JSONArray result = SNMPv3Request.getMac(SNMPParamFactory.createSNMPParam(networkElement));
            if (!result.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Mac> macList = objectMapper.readValue(result.toString(), new TypeReference<List<Mac>>(){});
                processMacData(networkElement, date, macList, hostName);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void getPortMacData(NetworkElement networkElement, Date date, String hostName){
        SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
        try {
//            JSONArray result = SNMPv2Request.getPortMac(snmpParams);
            JSONArray result = SNMPv3Request.getPortMac(SNMPParamFactory.createSNMPParam(networkElement));
            if (!result.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Mac> macList = objectMapper.readValue(result.toString(), new TypeReference<List<Mac>>(){});
                processPortMacData(networkElement, date, hostName, macList);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void processMacData(NetworkElement networkElement, Date date, List<Mac> macList, String hostName) {
        try {
            IMacService macService = (IMacService) ApplicationContextUtils.getBean("macServiceImpl");
            if (!macList.isEmpty()) {
                List<Mac> validMacList = new ArrayList<>();
                for (Mac mac : macList) {
                    if ("3".equals(mac.getType())) {
                        setMacDataFields(networkElement, date, mac, hostName);
                        if (isMacWithSpecificPrefix(mac.getMac())) {
                            mac.setTag("LV");
                        }
                        validMacList.add(mac);
                    }
                }
                if (!validMacList.isEmpty()) {
                    macService.batchSaveGather(validMacList);
                }
            }
        } catch (Exception e) {
            log.error("Error processing MAC data for IP: {}", networkElement.getIp(), e);
        }
    }

    private void processPortMacData(NetworkElement networkElement, Date date, String hostName, List<Mac> macList) {
        try {
            IMacService macService = (IMacService) ApplicationContextUtils.getBean("macServiceImpl");
            if (!macList.isEmpty()) {
                List<Mac> validMacList = new ArrayList<>();
                for (Mac mac : macList) {
                    if ("1".equals(mac.getStatus())) { // up 状态
                        setMacDataFields(networkElement, date, mac, hostName);
                        mac.setTag("L");
                        if (isMacWithSpecificPrefix(mac.getMac())) {
                            mac.setTag("LV");
                        }
                        validMacList.add(mac);
                    }
                }
                if (!validMacList.isEmpty()) {
                    macService.batchSaveGather(validMacList);
                }
            }
        } catch (Exception e) {
            log.error("Error processing port MAC data for IP: {}", networkElement.getIp(), e);
        }
    }


    private static final String MAC_PREFIX = "00:00:5e"; // 常量定义，避免硬编码

    private boolean isMacWithSpecificPrefix(String mac) {
        return mac != null && mac.startsWith(MAC_PREFIX);
    }

    // 设置 MAC 数据字段
    private void setMacDataFields(NetworkElement networkElement, Date date, Mac mac, String hostName) {
        mac.setAddTime(date);
        mac.setDeviceIp(networkElement.getIp());
        mac.setDeviceName(networkElement.getDeviceName());
        mac.setDeviceUuid(networkElement.getUuid());
        mac.setHostname(hostName);
        if (StringUtils.isNotEmpty(mac.getMac())) {
            mac.setMac1(MyStringUtils.getSubstringBeforNthDelimiter(mac.getMac(), ":", 3));
        }
    }

    private void setRemoteDevice(NetworkElement networkElement, List<Map> lldps, String hostname, Date date){
        IMacService macService = (IMacService) ApplicationContextUtils.getBean("macServiceImpl");
        // 判断 llpd 数据是否有效
        if (CollectionUtils.isNotEmpty(lldps)) {
            List<Mac> macList = new ArrayList<>();

            for (Map<String, String> lldp : lldps) {
                Mac mac = createMac(networkElement, lldp, hostname, date);
                macList.add(mac);
            }

            if (!macList.isEmpty()) {
                macService.batchSaveGather(macList);  // 批量保存
            }
        }
    }

    /**
     * 创建 Mac 对象
     *
     * @param e        网络元素
     * @param lldp     对端设备信息
     * @param hostname 主机名
     * @param date     当前时间
     * @return         Mac 对象
     */
    private Mac createMac(NetworkElement e, Map<String, String> lldp, String hostname, Date date) {
        Mac mac = new Mac();

        // 设置基本属性
        mac.setAddTime(date);
        mac.setDeviceIp(e.getIp());
        mac.setDeviceName(e.getDeviceName());
        mac.setDeviceUuid(e.getUuid());
        mac.setMac("00:00:00:00:00:00"); // 如果固定可以考虑移除或根据实际需求填充
        mac.setHostname(hostname);
        mac.setTag("DE");  // 远程设备的标记

        // 设置远程设备信息
        mac.setRemotePort(lldp.get("remoteport"));
        mac.setRemoteDevice(lldp.get("hostname"));

        // 如果 MAC 地址有效，进行截取前三段
        if (StringUtils.isNotEmpty(mac.getMac())) {
            mac.setMac1(MyStringUtils.getSubstringBeforNthDelimiter(mac.getMac(), ":", 3));
        }

        // 设置设备 UUID
        setMacDeviceUuid(e, mac);

        return mac;
    }

    /**
     * 设置 Mac 对应的设备 UUID
     *
     * @param e   网络元素
     * @param mac Mac 对象
     */
    private void setMacDeviceUuid(NetworkElement e, Mac mac) {
        INetworkElementService networkElementService = (INetworkElementService) ApplicationContextUtils.getBean("networkElementServiceImpl");
        Map<String, String> params = new HashMap<>();
        params.put("deviceIp", e.getIp());

        // 查找网络元素
        List<NetworkElement> networkElements = networkElementService.selectObjByMap(params);

        if (CollectionUtils.isNotEmpty(networkElements)) {
            mac.setDeviceUuid(networkElements.get(0).getUuid());
        }
    }
}
