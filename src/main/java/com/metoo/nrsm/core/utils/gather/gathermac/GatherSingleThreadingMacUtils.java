package com.metoo.nrsm.core.utils.gather.gathermac;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-23 10:39
 */
@Slf4j
@Component
public class GatherSingleThreadingMacUtils {

    @Autowired
    private IMacService macService;
    @Autowired
    private GatherMacUtils gatherMacUtils;
    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private ITerminalCountService terminalCountService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private PythonExecUtils pythonExecUtils;
    private static final String MAC_PREFIX = "00:00:5e"; // 常量定义，避免硬编码

    // 单线程采集
    public void gatherMac(List<NetworkElement> networkElements, Date date) {

        if (!networkElements.isEmpty()) {

            gatherMacUtils.copyGatherData(date);

            // 更新终端
            updateTerminal(date);

            macService.truncateTableGather();

            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion())
                        || StringUtils.isBlank(networkElement.getCommunity())){
                    continue;
                }

                String hostName = getHostName(networkElement);// 多余，查询设备时已经查询了是否存在

                if (StringUtils.isNotEmpty(hostName)) {

                    processNetworkElementData(networkElement, hostName, date);
                }
            }
        }
    }

    // 处理网络元素数据
    private void processNetworkElementData(NetworkElement networkElement, String hostName, Date date) {
        log.info("getlldp.py ===== {}", networkElement.getIp());
        getLldpData(networkElement, hostName, date);

        log.info("getmac.py ====={}", networkElement.getIp());
        getMacData(networkElement, hostName, date);

        log.info("getportmac.py ====={}", networkElement.getIp());
        getPortMacData(networkElement, hostName, date);
    }


    private String getHostName(NetworkElement networkElement){

        String path = Global.PYPATH + "gethostname.py";

        String[] params = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};

        String hostName = pythonExecUtils.exec2(path, params);

        return hostName;
    }

    public void getLldpData(NetworkElement networkElement, String hostName, Date date){

        String path = Global.PYPATH + "getlldp.py";

        String[] getlldpParams = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};

        String getlldp = pythonExecUtils.exec2(path, getlldpParams);

        if (StringUtil.isNotEmpty(getlldp)) {
            List<Map> lldps = JSONObject.parseArray(getlldp, Map.class);

            this.setRemoteDevice(networkElement, lldps, hostName, date);
        }
    }


    public void getMacData(NetworkElement networkElement, String hostName, Date date){

        String path = Global.PYPATH + "getmac.py";

        String[] getMacParams = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};

        String result = pythonExecUtils.exec2(path, getMacParams);

        if (StringUtil.isNotEmpty(result))
        {
            processMacData(networkElement, hostName, date, result);
        }
    }

    // 处理获取到的MAC数据
    private void processMacData(NetworkElement networkElement, String hostName, Date date, String result) {
        try {
            List<Mac> macList = JSONObject.parseArray(result, Mac.class);
            if (!macList.isEmpty()) {
                List<Mac> validMacList = new ArrayList<>();
                for (Mac mac : macList) {
                    if ("3".equals(mac.getType())) {
                        setMacDataFields(mac, networkElement, hostName, date);
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

    // 判断 MAC 是否以指定前缀开始
    private boolean isMacWithSpecificPrefix(String mac) {
        return mac != null && mac.startsWith(MAC_PREFIX);
    }

    // 设置 MAC 数据字段
    private void setMacDataFields(Mac mac, NetworkElement networkElement, String hostName, Date date) {
        mac.setAddTime(date);
        mac.setDeviceIp(networkElement.getIp());
        mac.setDeviceName(networkElement.getDeviceName());
        mac.setDeviceUuid(networkElement.getUuid());
        mac.setHostname(hostName);
        if (StringUtils.isNotEmpty(mac.getMac())) {
            mac.setMac1(MyStringUtils.getSubstringBeforNthDelimiter(mac.getMac(), ":", 3));
        }
    }

    public void getPortMacData(NetworkElement networkElement, String hostName, Date date){

        String path = Global.PYPATH + "getportmac.py";

        String[] params = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};

        String result = pythonExecUtils.exec2(path, params);

        if (StringUtil.isNotEmpty(result)) {
            processPortMacData(networkElement, hostName, date, result);
        }
    }

    // 处理获取到的端口MAC数据
    private void processPortMacData(NetworkElement networkElement, String hostName, Date date, String result) {
        try {
            List<Mac> macList = JSONObject.parseArray(result, Mac.class);
            if (!macList.isEmpty()) {
                List<Mac> validMacList = new ArrayList<>();
                for (Mac mac : macList) {
                    if ("1".equals(mac.getStatus())) { // up 状态
                        setMacDataFields(mac, networkElement, hostName, date);
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


    /**
     * 更新终端表
     * @param date
     */
    public void updateTerminal(Date date) {
        // 更新终端信息
        updateTerminalInfo(date);

        // 标记 deviceType 是否为设备（根据 mac 地址判断）
        markDeviceTypeByMac();

        // 统计终端所属单位
        writeTerminalUnitData();

        // 统计终端数量
        writeTerminalCountData(date);

        // 同步终端到历史表
        syncTerminalToHistory();
    }


    /**
     * 更新终端信息
     * （deviceType|deviceName|tag|网元display）
     * @param date
     */
    private void updateTerminalInfo(Date date) {
        try {
            terminalService.syncTerminal(date);

            // 政务外网
//          this.terminalService.v4Tov6Terminal(date);

            // 政务外网
            // 根据vendor判断终端类型
            this.terminalService.writeTerminalType();
            // 政务外网
            // 统计终端是否属于双栈终端
            this.terminalService.dualStackTerminal();

            updateTerminalDeviceTypeToNSwitch();

            terminalService.updateVMHostDeviceType();

            terminalService.updateVMDeviceType();

            terminalService.updateVMDeviceIp();

            networkElementService.updateObjDisplay();

        } catch (Exception e) {
            log.error("Error while updating terminal information", e);
        }
    }

    /**
     * deviceType 为1的终端设置为傻瓜交换机
     */
    private void updateTerminalDeviceTypeToNSwitch(){
        Map params = new HashMap();
        params.put("deviceType", 1);
        params.put("notDeviceTypeId", 36);
        params.put("online", true);
        List<Terminal> terminalList = this.terminalService.selectObjByMap(params);
        if(terminalList != null && !terminalList.isEmpty()){
            for (Terminal terminal : terminalList) {
                terminal.setDeviceTypeId(36L);
                this.terminalService.update(terminal);
            }
        }

        params.clear();
        params.put("online", true);
        params.put("notDeviceTypeId", 36);
        List<Terminal> terminals = this.terminalService.selectObjByMap(params);
        if(terminals != null && !terminals.isEmpty()){

            DeviceType deviceType1 = this.deviceTypeService.selectObjByType(14);
            DeviceType deviceType2 = this.deviceTypeService.selectObjByType(27);

            for (Terminal terminal : terminals) {
                if(StringUtils.isNotEmpty(terminal.getCombined_port_protocol())){
                    JSONArray jsonArray = JSONArray.parseArray(terminal.getCombined_port_protocol());
                    // 用于存储所有的端口号
                    Set<String> portNumbers = new HashSet<>();

                    // 遍历 JSON 数组，提取所有 port_num
                    for (int i = 0; i < jsonArray.size(); i++) {
                        String portNum = jsonArray.getJSONObject(i).getString("port_num");
                        if (StringUtils.isNotEmpty(portNum)) {
                            portNumbers.add(portNum.trim());  // 将 port_num 添加到集合中
                        }
                    }

                    // 判断是否有 23 端口
                    if (portNumbers.contains("23")) {
                        // 如果包含 23 端口，认为是网络设备，更新设备类型 ID
                        terminal.setDeviceTypeId(37L);  // NEW_NETWORK_DEVICE_TYPE_ID 是新增的网络设备 typeid
                    } else if (portNumbers.size() > 4) {
                        // 如果包含 23 端口，认为是网络设备，更新设备类型 ID
                        terminal.setDeviceTypeId(16L);  // NEW_NETWORK_DEVICE_TYPE_ID 是新增的网络设备 typeid
                    } else  if (portNumbers.contains("22")) {
                        // 如果包含 23 端口，认为是网络设备，更新设备类型 ID
                        terminal.setDeviceTypeId(16L);  // NEW_NETWORK_DEVICE_TYPE_ID 是新增的网络设备 typeid
                    } else if (portNumbers.contains("515")) {
                        // 如果包含 23 端口，认为是网络设备，更新设备类型 ID
                        terminal.setDeviceTypeId(19L);  // NEW_NETWORK_DEVICE_TYPE_ID 是新增的网络设备 typeid
                    } else {
                    // 恢复为普通终端
                    if (terminal.getType() == null || terminal.getType() == 0) {
                        if (StringUtils.isEmpty(terminal.getV4ip()) && StringUtils.isEmpty(terminal.getV6ip())) {
                            terminal.setDeviceTypeId(deviceType2.getId());
                        } else {
                            if(terminal.getDeviceTypeId() == null){
                                terminal.setDeviceTypeId(deviceType1.getId());
                            }
                        }
                    }
                }
                    this.terminalService.update(terminal);
                }

            }
        }


    }

    private void updateTerminalDeviceTypeTo(){
        Map params = new HashMap();
        params.put("deviceType", 1);
        params.put("notDeviceTypeId", 36);
        params.put("online", true);
        List<Terminal> terminalList = this.terminalService.selectObjByMap(params);
        if(terminalList != null && !terminalList.isEmpty()){
            for (Terminal terminal : terminalList) {
                terminal.setDeviceTypeId(36L);
                this.terminalService.update(terminal);
            }
        }
    }

    // 标记 deviceType 是否为设备
    private void markDeviceTypeByMac() {
        try {
            terminalService.updateObjDeviceTypeByMac();
        } catch (Exception e) {
            log.error("Error while updating device type by mac", e);
        }
    }

    // 统计终端单位数据
    private void writeTerminalUnitData() {
        try {
            terminalService.writeTerminalUnit();
            terminalService.writeTerminalUnitV6();
        } catch (Exception e) {
            log.error("Error while writing terminal unit data", e);
        }
    }

    // 统计终端数量并保存
    private void writeTerminalCountData(Date date) {
        try {
            Map terminalData = terminalService.terminalCount();
            TerminalCount count = new TerminalCount();
            count.setAddTime(date);

            if (terminalData != null) {
                count.setV4ip_count(Integer.parseInt(String.valueOf(terminalData.get("v4ip_count"))));
                count.setV6ip_count(Integer.parseInt(String.valueOf(terminalData.get("v6ip_count"))));
                count.setV4ip_v6ip_count(Integer.parseInt(String.valueOf(terminalData.get("v4ip_v6ip_count"))));
            } else {
                count.setV4ip_count(0);
                count.setV6ip_count(0);
                count.setV4ip_v6ip_count(0);
            }
            terminalCountService.save(count);
        } catch (Exception e) {
            log.error("Error while writing terminal count data", e);
        }
    }

    // 同步终端到终端历史表
    private void syncTerminalToHistory() {
        try {
            terminalService.syncTerminalToTerminalHistory();
        } catch (Exception e) {
            log.error("Error while syncing terminal to terminal history", e);
        }
    }


    private void setRemoteDevice(NetworkElement networkElement, List<Map> lldps, String hostname, Date date){

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
        Map<String, String> params = new HashMap<>();
        params.put("deviceIp", e.getIp());

        // 查找网络元素
        List<NetworkElement> networkElements = networkElementService.selectObjByMap(params);

        if (CollectionUtils.isNotEmpty(networkElements)) {
            mac.setDeviceUuid(networkElements.get(0).getUuid());
        }
    }

}