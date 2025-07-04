package com.metoo.nrsm.core.utils.gather.gathermac;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.manager.ap.utils.GecossApiUtil;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.service.impl.MacTestServiceImpl;
import com.metoo.nrsm.core.utils.mac.MacUtils;
import com.metoo.nrsm.entity.*;
import com.metoo.nrsm.entity.ac.AcAction;
import com.metoo.nrsm.entity.ac.AcUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sun.nio.ch.Net;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-23 10:46
 * <p>
 * Java事务注解失效的场景有以下几种：
 * <p>
 * 注解被错误使用：事务注解被错误地应用到非public方法上，或者被应用到一个没有被Spring容器管理的类上，这样会导致注解失效。
 * 异常被处理了：事务注解只在抛出未捕获的异常时才起作用，如果异常被捕获并处理了，事务注解可能会失效。
 * 异常被忽略了：在使用事务注解时，如果在方法中发生了异常但没有被捕获并抛出，或者异常被捕获后没有重新抛出，事务注解可能会失效。
 * 注解的生命周期不正确：事务注解的生命周期必须和Spring容器的生命周期保持一致，如果注解的生命周期不正确，事务注解可能会失效。
 * 配置错误：事务注解的配置可能会出现错误，例如事务的传播行为、隔离级别等配置错误，这样也会导致注解失效。
 * 不支持的事务管理器：某些事务管理器可能不支持某些注解，如果使用了不支持的事务管理器，事务注解可能会失效。
 * 缺少必要的配置：事务注解可能需要一些额外的配置才能正常工作，例如需要配置数据源、事务管理器等，如果缺少了必要的配置，事务注解可能会失效
 * mysql：如使用mysql且引擎是MyISAM，则事务会不起作用，原因是MyISAM不支持事务，可以改成InnoDB引擎
 */
@Slf4j
@Component
public class GatherMacUtils {
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private INswitchService nswitchService;
    @Autowired
    private IPortService portService;
    @Autowired
    private IMacService macService;

    @Autowired
    private MacTestServiceImpl macTestService;

    //    @Transactional
    public void copyGatherData(Date date) {
        try {
            copyData(date);
            List<Mac> macs = this.macService.selectObjByMap(Collections.emptyMap());
            if (!macs.isEmpty()) {// 给mac条目打tag
//                updateMacTag(date);
            }
        } catch (Exception e) {
            log.error("Error method copyGatherData: {}", date, e);
        }
    }

    private void copyData(Date date) {
        macService.copyGatherDataToMac(date);
    }

//    private void updateMacTag(Date date){
//        setTagToX(); // 读取mac表，与up接口的mac不重复标记为X，0:0:5e:0标记为V
//        setTagToU();// 标记U(1个mac对应1个port(除去L之外)，此条目标记为U)
//        setTagToS();
//        setTagSToE();
//        setTagSToRT();
//        setTagXToE();
//        setTagUToE();
//
//macTestService.executeFullProcess();
//
//
//        setTagUToRT();
////        RTToDT();
//        setTagRTToDT();
//        copyArpIpToMacByDT();
//        setTagRTToVDT(date);  // NSwitch
//        setTagDTToVDE(); // （无线路由器）
//        setTagRTToVDE();
//        setTagRTToDTByDE();
//        removeApTerminal(); // 删除mac与为ap mac地址相同的数据
//
//    }


    private void updateMacTag(Date date) {
        setTagToX(); // 读取mac表，与up接口的mac不重复标记为X，0:0:5e:0标记为V
        setTagToU();// 标记U(1个mac对应1个port(除去L之外)，此条目标记为U)
        setTagToS();
        setTagSToE();
        setTagXToE();
        setTagUToE();

        macTestService.executeFullProcess();

        setTagSToRT();
        setTagUToRT();
//        RTToDT(); // 弃用
        setTagRTToDT();
        copyArpIpToMacByDT();
        setTagRTToVDT(date);  // NSwitch
        setTagDTToVDE(); // （无线路由器）
        setTagRTToVDE();
        setTagRTToDTByDE();

        // DE条目remotePort修改为remoteIp对应的deviceIp的port，再在DE里面根据deviceIp和remoteIp去重
        normalizePortForDE();

        removeApTerminal(); // 删除mac与为ap mac地址相同的数据

        selectSameSubnetWithTwoPortsNotBothVlan(date);// 生成DE

    }

    /**
     * 在同一网段
     * <p>
     * 网段内仅有两条记录
     * <p>
     * 两个 port 不同时以 vlan 开头
     * <p>
     * 并且：A 的 remoteUuid = B.deviceUuid B 的 remoteUuid = A.deviceUuid
     *
     * @param date
     */
//    public void selectSameSubnetWithTwoPortsNotBothVlan(Date date) {
//        List<Port> ports = this.portService.selectSameSubnetWithTwoPortsNotBothVlan();
//
//        if(ports.size() > 0){
//            Map params = new HashMap();
//            for (Port port : ports) {
//                String deviceIp = port.getIp();
//                String portName = port.getPort();
//                params.put("deviceIp", deviceIp);
//                params.put("portName", portName);
//                params.put("tag", "DE");
//                List<Mac> macs = this.macService.selectObjByMap(params);
//                if(macs.size() <= 0){
//                    try {
//                        Mac mac = new Mac();
//                        mac.setAddTime(date);
//                        mac.setMac("00:00:00:00:00:00");
//                        mac.setDeviceUuid(port.getDeviceUuid());
//                        mac.setPort(portName);
//                        mac.setDeviceIp(deviceIp);
//                        mac.setTag("DE");
//                        NetworkElement networkElement = this.networkElementService.selectObjByUuid(port.getDeviceUuid());
//                        if(networkElement != null){
//                            mac.setDeviceName(networkElement.getDeviceName());
//                        }
//                        this.macService.save(mac);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
    public void selectSameSubnetWithTwoPortsNotBothVlan(Date date) {
        List<Port> ports = this.portService.selectSameSubnetWithTwoPortsNotBothVlan();
        // 按 networkAddress 分组
        Map<String, List<Port>> subnetMap = new HashMap<>();
        for (Port port : ports) {
            subnetMap.computeIfAbsent(port.getNetworkAddress(), k -> new ArrayList<>())
                    .add(port);
        }
        Map params = new HashMap();
        // 遍历每组，仅处理正好两条记录的 subnet
        for (Map.Entry<String, List<Port>> entry : subnetMap.entrySet()) {
            List<Port> pair = entry.getValue();
            if (pair.size() != 2) continue; // 只处理刚好两条的网段

            Port a = pair.get(0);
            Port b = pair.get(1);

            NetworkElement a_Network = this.networkElementService.selectObjByUuid(a.getDeviceUuid());
            NetworkElement b_Network = this.networkElementService.selectObjByUuid(b.getDeviceUuid());

            String a_deviceIp = a_Network.getIp();
            String b_deviceIp = b_Network.getIp();

            // 设置 remoteUuid
            a.setIp(a_deviceIp);
            a.setRemoteUuid(b.getDeviceUuid());
            a.setRemotePort(b.getPort());
            a.setRemoteIp(b_deviceIp);
            b.setIp(b_deviceIp);
            b.setRemoteUuid(a.getDeviceUuid());
            b.setRemotePort(a.getPort());
            b.setRemoteIp(a_deviceIp);

            insertMac(a, params, date);
            insertMac(b, params, date);
        }
    }

    public void insertMac(Port port, Map params, Date date) {
        params.clear();
        String deviceIp = port.getIp();
        String portName = port.getPort();
        params.put("deviceIp", deviceIp);
        params.put("remoteIp", port.getRemoteIp());
        params.put("portName", portName);
        params.put("tag", "DE");
        List<Mac> macs = this.macService.selectObjByMap(params);
        if (macs.size() <= 0) {
            try {
                Mac mac = new Mac();
                mac.setAddTime(date);
                mac.setMac("00:00:00:00:00:00");
                mac.setDeviceUuid(port.getDeviceUuid());
                mac.setPort(portName);
                mac.setDeviceIp(deviceIp);
                mac.setRemotePort(port.getRemotePort());
                mac.setTag("DE");
                NetworkElement networkElement = this.networkElementService.selectObjByUuid(port.getDeviceUuid());
                if (networkElement != null) {
                    mac.setDeviceName(networkElement.getDeviceName());
                }
                NetworkElement remoteDevice = this.networkElementService.selectObjByUuid(port.getRemoteUuid());
                if (remoteDevice != null) {
                    mac.setRemoteDevice(remoteDevice.getDeviceName());
                    mac.setRemoteDeviceName(remoteDevice.getDeviceName());
                    mac.setRemoteDeviceUuid(remoteDevice.getUuid());
                    mac.setRemoteIp(port.getRemoteIp());
                }
                this.macService.save(mac);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // DE条目remotePort修改为remoteIp对应的deviceIp的port
    public void normalizePortForDE() {

        this.macService.normalizePortForDE();

        safelyDeleteDuplicateDEIpPairs();
    }

    // 删除DE重复数据（deviceIp + remoteIp）
    public void safelyDeleteDuplicateDEIpPairs() {
        this.macService.safelyDeleteDuplicateDEIpPairs();
    }


    // 标记为X
    public void setTagToX() {// 单台设备
        List<Mac> unequalToUpMac = macService.selectTagToX(null);
        if (unequalToUpMac.size() > 0) {
            unequalToUpMac.stream().forEach(e -> e.setTag(setTag("X", e.getMac())));
            macService.batchUpdate(unequalToUpMac);
        }
    }

    // 根据mac地址，标记V
    private String setTag(String tag, String mac) {
        String patten = "^" + "00:00:5e";
        boolean flag = this.parseLineBeginWith(mac, patten);
        if (flag) {
            return "V";
        }
        return tag;
    }

    /**
     * 判断Mac是否以某个规则开始
     *
     * @param lineText
     * @param head
     * @return
     */
    private boolean parseLineBeginWith(String lineText, String head) {

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

    // 标记U(1个mac对应1个port(除去L之外)，此条目标记为U)
    public void setTagToU() {// 单台设备
        List<Mac> macs = this.macService.selectTagToU(null);
        if (macs.size() > 0) {
            macs.stream().forEach(e -> e.setTag("U"));
            this.macService.batchUpdate(macs);
        }
    }

    public void setTagToS() {
        List<Mac> macs = this.macService.selectTagToS(null);
        if (macs.size() > 0) {
            macs.stream().forEach(e -> e.setTag("S"));
            this.macService.batchUpdate(macs);
        }
    }

    public void setTagSToE() {// 单台设备
        List<Mac> macs = this.macService.selectTagSToE(null);
        if (macs.size() > 0) {
            macs.stream().forEach(e -> e.setTag("E"));
            this.macService.batchUpdate(macs);
        }
    }

    public void setTagSToRT() {// 单台设备
        List<Mac> macs = this.macService.selectTagSToRT(null);
        if (macs.size() > 0) {
            macs.stream().forEach(e -> e.setTag("RT"));
            this.macService.batchUpdate(macs);
        }
    }

    // 将arp表中mac对应的ip地址、mac厂商写入mac表(不包含DE)
    public void copyArpMacAndIpToMac() {
        try {
            List<Mac> macs = this.macService.copyArpMacAndIpToMac(null);
            if (macs != null && macs.size() > 0) {
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 为x的条目如果在全网(除本机)匹配到任何1条为L的标记，则此条目标记为E(Equipment)
    public void setTagXToE() {
        try {
            List<Mac> macs = this.macService.selectXToEByMap(null);
            if (macs != null && macs.size() > 0) {
                macs.stream().forEach(e -> e.setTag("E"));
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTagUToE() {
        try {
            List<Mac> macs = this.macService.selectUToEByMap(null);
            if (macs != null && macs.size() > 0) {
                macs.stream().forEach(e -> e.setTag("E"));
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTagUToRT() {
        try {
            List<Mac> macs = this.macService.selectUToRTByMap(null);
            if (macs != null && macs.size() > 0) {
                Set set1 = macs.stream().map(e -> e.getId()).collect(Collectors.toSet());
                Set set2 = macs.stream().flatMap(e -> e.getMacList().stream()).map(e -> e).collect(Collectors.toSet());
                set1.addAll(set2);
                this.macService.updateMacTagToRTByIds(set1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 标记为UT且有ip地址的，标记为DT
    public void RTToDT() {
        try {
            List<Mac> macs = this.macService.selectRTToDTByMap(null);
            if (macs != null && macs.size() > 0) {
                Set set1 = macs.stream().map(e -> e.getId()).collect(Collectors.toSet());
                this.macService.updateMacTagToDTByIds(set1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTagRTToDT() {
        try {
            List<Mac> macs = this.macService.selectRTToDT2ByMap(null);
            if (macs != null && macs.size() > 0) {
                Set set1 = macs.stream().map(e -> e.getId()).collect(Collectors.toSet());
                this.macService.updateMacTagToDTByIds(set1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyArpIpToMacByDT() {
        try {
            List<Mac> macs = this.macService.copyArpIpToMacByDT(null);
            if (macs != null && macs.size() > 0) {
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTagDTToVDE() {
        try {
            List<Mac> macs = this.macService.selectDTToDEByMap(null);
            if (macs != null && macs.size() > 0) {
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTagRTToVDE() {
        try {
            List<Mac> macs = this.macService.selectRTToDEByMap(null);
            if (macs != null && macs.size() > 0) {
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTagRTToDTByDE() {
        try {
            List<Mac> macs = this.macService.selectRTToDTByDE();
            if (macs != null && macs.size() > 0) {
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTagRTToVDT(Date date) {
        try {
            List<Mac> macs = this.macService.selectRTToVDT();
            int i = 0;
            if (macs != null && !macs.isEmpty()) {
                Set ports = new HashSet();
                for (Mac mac : macs) {
                    String nswitchName = "";
                    String name = mac.getDeviceIp() + mac.getPort();
                    Nswitch nswitch = this.nswitchService.selectObjByName(name);
                    if (nswitch != null) {
                        nswitchName = "NSwitch" + nswitch.getIndex();
                    } else {
                        List<Nswitch> nswitchs = this.nswitchService.selectObjAll();
                        Integer index = 1;
                        if (nswitchs.size() > 0) {
                            Nswitch nswitch1 = nswitchs.get(0);
                            index = nswitch1.getIndex() + index;
                            nswitchName = "NSwitch" + index;
                        } else {
                            nswitchName = "NSwitch" + index;
                        }
                        Nswitch nswitch2 = new Nswitch();
                        nswitch2.setName(name);
                        nswitch2.setIndex(index);
                        this.nswitchService.save(nswitch2);
                    }
                    if (!ports.contains(mac.getPort())) {
                        i++;
                        ports.add(mac.getPort());
                        Mac obj = new Mac();
                        obj.setAddTime(date);
                        obj.setMac("00:00:00:00:00:00");
                        obj.setTag("DE");
                        obj.setPort(mac.getPort());
                        obj.setDeviceIp(mac.getDeviceIp());
                        obj.setDeviceUuid(mac.getDeviceUuid());
                        obj.setDeviceName(mac.getDeviceName());
                        obj.setHostname(mac.getHostname());
                        // 对端设备信息
                        obj.setRemotePort("V0");
                        obj.setRemoteDevice(nswitchName);
                        this.macService.save(obj);

//                        // 增加虚拟网元
                        Map params = new HashMap();
                        params.put("deviceName", nswitchName);
                        params.put("display", 1);
                        List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
                        if (networkElements.size() <= 0) {
                            NetworkElement ne = new NetworkElement();
                            ne.setAddTime(new Date());
                            ne.setDisplay(true);
                            ne.setDeviceName(nswitchName);
                            DeviceType deviceType = this.deviceTypeService.selectObjByType(29);
                            ne.setDeviceTypeId(deviceType.getId());
                            this.networkElementService.save(ne);
                        }
                    }

                    mac.setDeviceIp2(mac.getDeviceIp());
                    mac.setDeviceName2(mac.getDeviceName());
                    mac.setDevicePort2(mac.getPort());

                    mac.setTag("DT");
                    mac.setPort("V1");
                    mac.setDeviceName(nswitchName);
                    mac.setHostname(nswitchName);
                    mac.setDeviceIp(null);

                    // 查询ap用户列表
                    // 验证api是否可用

//                    boolean flag = this.setDevice(mac);
//                    if(!flag){
//                        mac.setDeviceName(ns);
//                        mac.setHostname(ns);
//                    }else{
//                        mac.setDeviceIp(null);
//                    }
//                    mac.setDeviceName("NSwitch" + i);
//                    mac.setHostname("NSwitch" + i);

                    this.macService.update(mac);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 设置ac用户列表信息到终端设备
    public boolean setDevice(Mac mac) {
        AcUser instance = new AcUser();
        instance.setPagenum(1);
        instance.setNumperpage(10000000);
        JSONObject result = null;
        try {
            result = GecossApiUtil.getCall(GecossApiUtil.parseParam(instance, "stasearch"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.size() <= 0) {
            return false;
        }
        JSONObject jsonObject = result;
        boolean flag = false;
        if (StringUtils.isNotBlank(String.valueOf(jsonObject.get("stalist")))/*jsonObject.get("stalist") != null && Strings.isNotBlank(String.valueOf(jsonObject.get("stalist")))*/) {
            JSONArray jsonArray = jsonObject.getJSONArray("stalist");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    if (StringUtils.isNotBlank(obj.getString("mac"))) {
                        if (mac.getMac().equalsIgnoreCase(obj.getString("mac"))) {
//                            mac.setDeviceIp(obj.getString("ip"));
                            mac.setDeviceName(obj.getString("name"));
                            mac.setHostname(obj.getString("name"));
                            mac.setDeviceIp(null);
                            flag = true;
                            break;
                        }
                    }
                }
            }
        }
        if (!flag) {
            mac.setDeviceIp(null);
        }
        return flag;
    }


    /**
     * mac对端设备
     *
     * @param e
     * @param lldps
     * @param hostname
     * @param date
     */
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
                mac.setRemoteDevice(obj.get("hostname"));
                this.macService.save(mac);
            }
        }
    }

    public void removeApTerminal() {
    }/*{
        Map params = new HashMap();
        params.put("type", 3);
        List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
        if(networkElements.size() > 0){
//            Set set = new HashSet();
//            for (NetworkElement networkElement : networkElements) {
//                set.add(networkElement.getIp());
//            }
//
//            if(set.size() > 0){
                AcAction instance = new AcAction();
                instance.setNumperpage(1000000);
                instance.setPagenum(1);
                JSONObject jsonObject = GecossApiUtil.getCall(GecossApiUtil.parseParam(instance, "apsearch"));
                if(Strings.isNotBlank(String.valueOf(jsonObject.get("aplist")))){
                    JSONArray jsonArray = JSONObject.parseArray(String.valueOf(jsonObject.get("aplist")));
                    if(jsonArray != null){
                        params.clear();
                        for (Object ele : jsonArray) {
                            JSONObject obj = JSONObject.parseObject(String.valueOf(ele));
                            String name = obj.getString("name");
                            String ip = obj.getString("ip");
                            if(Strings.isNotBlank(name) && Strings.isNotBlank(ip)){
                                params.clear();
                                params.put("deviceName", name);
                                params.put("ip", ip);
                                params.put("type", 3);
                                List<NetworkElement> list = this.networkElementService.selectObjByMap(params);
                                if(list.size() > 0){
                                    params.clear();
                                    String macWithColons = MacUtils.formatMacAddress(obj.getString("mac"));
                                    if(StringUtils.isNotBlank(macWithColons)){
                                        params.put("mac", macWithColons);
                                        List<Terminal> terminals = this.terminalService.selectObjByMap(params);
                                        if(terminals.size() > 0){
                                            for (Terminal terminal : terminals) {
                                                this.terminalService.delete(terminal.getId());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
//                }
            }
        }
    }*/

}
