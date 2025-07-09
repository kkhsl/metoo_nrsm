package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.dto.MacDTO;
import com.metoo.nrsm.core.mapper.MacMapper;
import com.metoo.nrsm.core.service.IArpService;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.IMacService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Arp;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:20
 */

@Service
@Transactional
public class MacServiceImpl implements IMacService {

    @Autowired
    private MacMapper macMapper;
    @Autowired
    private IArpService arpService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private PythonExecUtils pythonExecUtils;
    @Autowired
    private IDeviceTypeService deviceTypeService;

    @Override
    public List<Mac> selectObjByMap(Map params) {
        return this.macMapper.selectObjByMap(params);
    }

    @Override
    public Page<Mac> selectByUuid(MacDTO instance) {
        Map map=new HashMap();
        map.put("deviceUuid",instance.getDeviceUuid());
        Page<Mac> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        macMapper.selectObjByMap(map);
        return page;
    }


    /**
     * 查询tag为DE条目
     * # 修改为，不为NSwitch的DE条目
     *
     * @return
     */
    @Override
    public List<Mac> selectTagByDE() {
        return this.macMapper.selectTagByDE();
    }

    @Override
    public List<Mac> selectTagDEWithoutNswitch() {
        List<Mac> list = this.macMapper.selectTagDEWithoutNswitch();
        list = filterMirrorData(list);
        if (list.size() > 0) {
            list = macDataSupplement(list);
        }
        return list;
    }

    @Override
    public List<Mac> selectTagDEWithNswitch() {
        List<Mac> list = this.macMapper.selectTagDEWithNswitch();
        list = filterMirrorData(list);
        if (list.size() > 0) {
            list = macDataSupplement(list);
        }
        return list;
    }

    private List<Mac> macDataSupplement(List<Mac> list) {
        Map params = new HashMap();
        for (Mac de : list) {
            params.clear();
            if (StringUtil.isNotEmpty(de.getDeviceIp())) {
                params.put("ip", de.getDeviceIp());
                List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
                if (networkElements.size() > 0) {
                    NetworkElement networkElement = networkElements.get(0);
                    if (networkElement.getDeviceTypeId() != null) {
                        DeviceType deviceType = this.deviceTypeService.selectObjById(networkElement.getDeviceTypeId());
                        de.setDeviceTypeUuid(deviceType.getUuid());
                        de.setDeviceType(deviceType.getName());
                    }
                    if (StringUtil.isNotEmpty(de.getRemoteDevice())) {
                        params.clear();
                        params.put("hostname", de.getRemoteDevice());
                        List<Mac> remoteDeviceList = this.macMapper.selectObjByMap(params);
                        if (remoteDeviceList.size() > 0) {
                            Mac remoteDevice = remoteDeviceList.get(0);
                            de.setRemoteDeviceIp(remoteDevice.getDeviceIp());
                            de.setRemoteDeviceName(remoteDevice.getDeviceName());
                            params.clear();
                            params.put("hostname", de.getRemoteDevice());
                            params.put("remoteDevice", de.getHostname());
                            List<Mac> portMac = this.macMapper.selectObjByMap(params);
                            if (portMac.size() > 0) {
                                de.setPort(portMac.get(0).getRemotePort());
                            }
                            if (StringUtil.isNotEmpty(remoteDevice.getDeviceIp())) {
                                params.clear();
                                params.put("ip", remoteDevice.getDeviceIp());
                                List<NetworkElement> remote_networkElements = this.networkElementService.selectObjByMap(params);
                                if (remote_networkElements.size() > 0) {
                                    NetworkElement remote_networkElement = remote_networkElements.get(0);
                                    de.setRemoteDeviceUuid(remote_networkElement.getUuid());
                                    if (remote_networkElement.getDeviceTypeId() != null) {
                                        DeviceType deviceType = this.deviceTypeService.selectObjById(remote_networkElement.getDeviceTypeId());
                                        de.setRemoteDevicTypeeUuid(deviceType.getUuid());
                                        de.setRemoteDeviceType(deviceType.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (StringUtil.isNotEmpty(de.getRemoteDevice())) {
                params.clear();
                params.put("deviceName", de.getDeviceName());
                params.put("display", 1);
                List<NetworkElement> NSwitch_nes2 = this.networkElementService.selectObjByMap(params);
                if (NSwitch_nes2.size() > 0) {
                    NetworkElement NSwitch_ne = NSwitch_nes2.get(0);
                    de.setDeviceUuid(NSwitch_ne.getUuid());
                    if (NSwitch_ne.getDeviceTypeId() != null) {
                        DeviceType deviceType = this.deviceTypeService.selectObjById(NSwitch_ne.getDeviceTypeId());
                        de.setDeviceTypeUuid(deviceType.getUuid());
                        de.setDeviceType(deviceType.getName());
                    }
                }
            }
            if (StringUtil.isNotEmpty(de.getRemoteDevice())) {
                params.clear();
                params.put("deviceName", de.getRemoteDevice());
                params.put("displayList", Arrays.asList(0, 1));
                List<NetworkElement> NSwitch_nes = this.networkElementService.selectObjByMap(params);
                if (NSwitch_nes.size() > 0) {
                    NetworkElement NSwitch_ne = NSwitch_nes.get(0);
                    de.setRemoteDeviceUuid(NSwitch_ne.getUuid());
                    de.setRemoteDeviceIp(NSwitch_ne.getIp());
                    de.setRemoteDeviceName(NSwitch_ne.getDeviceName());
                    if (NSwitch_ne.getDeviceTypeId() != null) {
                        DeviceType deviceType = this.deviceTypeService.selectObjById(NSwitch_ne.getDeviceTypeId());
                        de.setRemoteDevicTypeeUuid(deviceType.getUuid());
                        de.setRemoteDeviceType(deviceType.getName());
                    }
                }
            }
        }
        return list;
    }

    /**
     * TODO: 2025/1/6 过滤镜像数据 SQL
     * <p>
     * 过滤镜像数据
     *
     * @param list
     * @return
     */
    private List<Mac> filterMirrorData(List<Mac> list) {
        if (list != null && !list.isEmpty()) {
            // 使用 Set 来存储已处理的唯一键
            Set<String> uniqueDevices = new HashSet<>();
            List<Mac> filteredList = new ArrayList<>();
            for (Mac mac : list) {
                String remoteDevice = mac.getHostname() + mac.getRemoteDevice();
                String hostName = mac.getRemoteDevice() + mac.getHostname();
                if (!uniqueDevices.contains(remoteDevice) && !uniqueDevices.contains(hostName)) {
                    uniqueDevices.add(remoteDevice); // 将 remoteDevice 添加到集合
                    filteredList.add(mac); // 加入结果列表
                }
            }
            return filteredList; // 返回新的过滤后的列表
        }
        return list;
    }

    @Override
    public List<Mac> selectTagToX(Map params) {
        return this.macMapper.selectTagToX(params);
    }

    @Override
    public List<Mac> selectTagToU(Map params) {
        return this.macMapper.selectTagToU(params);
    }

    @Override
    public List<Mac> selectTagToS(Map params) {
        return this.macMapper.selectTagToS(params);
    }

    @Override
    public List<Mac> selectTagSToE(Map params) {
        return this.macMapper.selectTagSToE(params);
    }

    @Override
    public List<Mac> selectTagSToRT(Map params) {
        return this.macMapper.selectTagSToRT(params);
    }

    @Override
    public List<Mac> selectDistinctObjByMap(Map params) {
        return this.macMapper.selectDistinctObjByMap(params);
    }

    @Override
    public List<Mac> copyArpMacAndIpToMac(Map params) {
        return this.macMapper.copyArpMacAndIpToMac(params);
    }

    @Override
    public List<Mac> selectXToEByMap(Map params) {
        return this.macMapper.selectXToEByMap(params);
    }

    @Override
    public List<Mac> selectUToEByMap(Map params) {
        return this.macMapper.selectUToEByMap(params);
    }

    @Override
    public List<Mac> selectXToUTByMap(Map params) {
        return this.macMapper.selectXToUTByMap(params);
    }

    @Override
    public List<Mac> selectUToRTByMap(Map params) {
        return this.macMapper.selectUToRTByMap(params);
    }

    @Override
    public List<Mac> selectRTToDTByMap(Map params) {
        return this.macMapper.selectRTToDTByMap(params);
    }

    @Override
    public List<Mac> selectRTToDT2ByMap(Map params) {
        return this.macMapper.selectRTToDT2ByMap(params);
    }

    @Override
    public List<Mac> selectDTByMap(Map params) {
        return this.macMapper.selectDTByMap(params);
    }

    @Override
    public List<Mac> copyArpIpToMacByDT(Map params) {
        return this.macMapper.copyArpIpToMacByDT(params);
    }

    @Override
    public List<Mac> selectDTToDEByMap(Map params) {
        return this.macMapper.selectDTToDEByMap(params);
    }

    @Override
    public List<Mac> selectRTToDEByMap(Map params) {
        return this.macMapper.selectRTToDEByMap(params);
    }

    @Override
    public List<Mac> selectRTToDTByDE() {
        return this.macMapper.selectRTToDTByDE();
    }

    @Override
    public List<Mac> selectRTToVDT() {
        return this.macMapper.selectRTToVDT();
    }

    @Override
    public List<Mac> selectDTAndDynamicByMap(Map params) {


        return this.macMapper.selectDTAndDynamicByMap(params);
    }

    @Override
    public Page<Mac> selectDTAndDynamicByConditionQuery(MacDTO instance) {
        if (instance == null) {
            instance = new MacDTO();
        }
        Page<Mac> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.macMapper.selectDTAndDynamicByConditionQuery(instance);
        return page;
    }

    @Override
    public boolean save(Mac instance) {
        try {
            this.macMapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Mac instance) {
        try {
            this.macMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateMacTagToRTByIds(Set<Long> ids) {
        try {
            this.macMapper.updateMacTagToRTByIds(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateMacTagToDTByIds(Set<Long> ids) {
        try {
            this.macMapper.updateMacTagToDTByIds(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int normalizePortForDE() {
        try {
            return this.macMapper.normalizePortForDE();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int safelyDeleteDuplicateDEIpPairs() {
        try {
            return this.macMapper.safelyDeleteDuplicateDEIpPairs();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean batchSave(List<Mac> instance) {
        try {
            this.macMapper.batchSave(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchUpdate(List<Mac> instance) {
        try {
            this.macMapper.batchUpdate(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean truncateTable() {
        try {
            this.macMapper.truncateTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        String getlldp = "[{\"hostname\": \"core_sw2\", \"localport\": \"GigabitEthernet0/1\", \"remoteport\": \"GigabitEthernet0/1\"}, {\"hostname\": \"jr_sw3\", \"localport\": \"GigabitEthernet0/5\", \"remoteport\": \"GigabitEthernet0/1\"}, {\"hostname\": \"jr_sw2\", \"localport\": \"GigabitEthernet0/2\", \"remoteport\": \"GigabitEthernet0/2\"}, {\"hostname\": \"jr_sw4\", \"localport\": \"GigabitEthernet0/3\", \"remoteport\": \"GigabitEthernet0/3\"}]";

        String a = "[\"a\",\"b\"]";
//        List list = Arrays.asList(getlldp);
//        System.out.println(list);
        List lldps = JSONObject.parseArray(getlldp, Map.class);
        System.out.println(lldps);
    }

    @Override
    public void gatherMac(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if (networkElements.size() > 0) {
            this.macMapper.truncateTable();
            for (NetworkElement networkElement : networkElements) {
                String path = Global.PYPATH + "gethostname.py";
                String[] params1 = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String hostname = pythonExecUtils.exec(path, params1);

                // mac表增加remote-device，remote-port
                try {
                    path = Global.PYPATH + "getlldp.py";
                    String[] params3 = {networkElement.getIp(), networkElement.getVersion(),
                            networkElement.getCommunity()};
                    String getlldp = pythonExecUtils.exec(path, params3);
                    List<Map> lldps = JSONObject.parseArray(getlldp, Map.class);
                    this.setRemoteDevice(networkElement, lldps, hostname, date);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                path = Global.PYPATH + "getmac.py";
                // String result = PythonExecUtils.exec(path);
                String[] params = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String result = pythonExecUtils.exec(path, params);
                if (!"".equals(result)) {
                    try {


                        List<Mac> array = JSONObject.parseArray(result, Mac.class);
                        if (array.size() > 0) {
                            array.forEach(e -> {
                                if ("3".equals(e.getType())) {
                                    e.setDeviceIp(networkElement.getIp());
                                    e.setDeviceName(networkElement.getDeviceName());
                                    e.setAddTime(date);
                                    e.setHostname(hostname);
//                                    e.setTag("L");
                                    String patten = "^" + "00:00:5e";
                                    boolean flag = this.parseLineBeginWith(e.getMac(), patten);
                                    if (flag) {
                                        e.setTag("LV");
                                    }

                                    this.macMapper.save(e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                path = Global.PYPATH + "getportmac.py";
                // String result = PythonExecUtils.exec(path);
                String[] params2 = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                result = pythonExecUtils.exec(path, params2);
                if (!"".equals(result)) {
                    try {
                        List<Mac> array = JSONObject.parseArray(result, Mac.class);
                        if (array.size() > 0) {
                            array.forEach(e -> {
                                if ("1".equals(e.getStatus())) {
                                    e.setAddTime(date);
                                    e.setDeviceIp(networkElement.getIp());
                                    e.setDeviceName(networkElement.getDeviceName());
                                    e.setTag("L");
                                    e.setHostname(hostname);
                                    String patten = "^" + "00:00:5e";
                                    boolean flag = this.parseLineBeginWith(e.getMac(), patten);
                                    if (flag) {
                                        e.setTag("LV");
                                    }
                                    this.macMapper.save(e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    @Override
    public boolean saveGather(Mac instance) {
        try {
            this.macMapper.saveGather(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchSaveGather(List<Mac> instance) {
        try {
            this.macMapper.batchSaveGather(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean truncateTableGather() {
        try {
            this.macMapper.truncateTableGather();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTable() {
        try {
            this.macMapper.deleteTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean copyGatherDataToMac(Date date) {
        try {
            this.macMapper.deleteTable();
            this.macMapper.copyGatherDataToMac(date);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean copyGather(Date date) {
        try {

            int ii = this.macMapper.copyGatherDataToMac(date);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void lock() {
        this.macMapper.lock();
    }

    @Override
    public void releaseLock() {
        this.macMapper.releaseLock();
    }

    @Override
    public int queryLock() {
        return this.macMapper.queryLock();
    }

    @Override
    public int copyDataToMacHistory() {
        return this.macMapper.copyDataToMacHistory();
    }


    // mac对端设备
//    @Deprecation
//    public void setRemoteDevice(Mac e, Map de_weight, List<Map> lldps){
//        // 写入对端信息
//        if(de_weight.get(e.getPort()) == null || de_weight.get(e.getPort()).equals("")){
//            if(lldps != null && lldps.size() > 0){
//                for(Map<String, String> obj : lldps){
//                    String localport = obj.get("localport").toString();
//                    if(localport.equals(e.getPort())){
//                        e.setRemote_port(obj.get("remoteport").toString());
//                        e.setRemote_device(obj.get("hostname").toString());
//                        de_weight.put(e.getPort(), 1);
//                    }
//                }
//            }
//        }
//    }

    // mac对端设备
    public void setRemoteDevice(NetworkElement e, List<Map> lldps, String hostname, Date date) {
        // 写入对端信息
        if (lldps != null && lldps.size() > 0) {
            for (Map<String, String> obj : lldps) {
                Mac mac = new Mac();
                mac.setAddTime(date);
                mac.setDeviceIp(e.getIp());
                mac.setDeviceName(e.getDeviceName());
                mac.setDeviceUuid(e.getUuid());
//                mac.setPort(e.getPort());
                mac.setMac("00:00:00:00:00:00");
                mac.setHostname(hostname);
                mac.setTag("DE");
                mac.setRemotePort(obj.get("remoteport"));
                mac.setRemoteDeviceName(obj.get("hostname"));
                this.macMapper.save(mac);
            }
        }
    }

    public boolean parseLineBeginWith(String lineText, String head) {

        if (StringUtil.isNotEmpty(lineText) && StringUtil.isNotEmpty(head)) {
            String patten = "^" + head;

            Pattern compiledPattern = Pattern.compile(patten);

            Matcher matcher = compiledPattern.matcher(lineText);

            while (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    // 将arp表中mac对应的ip地址写入mac表：mac+port+deviceip
    public String getArpIp(String mac, String port, String deviceIp) {
        Map params = new HashMap();
        List<Arp> arps = this.arpService.selectObjByMap(params);
        if (arps.size() > 0) {
            return arps.get(0).getDeviceIp();
        }
        return "";
    }
}
