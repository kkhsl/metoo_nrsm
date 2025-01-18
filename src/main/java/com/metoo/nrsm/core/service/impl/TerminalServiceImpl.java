package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.core.manager.utils.MacUtils;
import com.metoo.nrsm.core.manager.utils.TerminalUtils;
import com.metoo.nrsm.core.manager.utils.VerifyMacVendorUtils;
import com.metoo.nrsm.core.mapper.MacVendorMapper;
import com.metoo.nrsm.core.mapper.TerminalMacIpv6Mapper;
import com.metoo.nrsm.core.mapper.TerminalMacVendorMapper;
import com.metoo.nrsm.core.mapper.TerminalMapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.ip.ipv4.IpSubnetMap;
import com.metoo.nrsm.core.utils.ip.ipv4.Ipv6SubnetMap;
import com.metoo.nrsm.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.UnknownHostException;
import java.util.*;

@Service
@Transactional
public class TerminalServiceImpl implements ITerminalService {

    @Autowired
    private TerminalMapper terminalMapper;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private IPortService portService;
    @Autowired
    private IPortIpv6Service portIpv6Service;
    @Autowired
    private ITerminalUnitService terminalUnitService;
    @Autowired
    private ITerminalUnitSubnetService terminalUnitSubnetService;
    @Autowired
    private ITerminalUnitSubnetV6Service terminalUnitSubnetV6Service;
    @Autowired
    private TerminalMacIpv6Mapper terminalMacIpv6Mapper;
    @Autowired
    private MacVendorMapper macVendorMapper;
    @Autowired
    private TerminalMacVendorMapper terminalMacVendorMapper;
    @Autowired
    private IArpService arpService;

    @Override
    public Terminal selectObjById(Long id) {
        return this.terminalMapper.selectObjById(id);
    }

    @Override
    public Page<Terminal> selectObjByConditionQuery(TerminalDTO instance) {
        if (instance == null) {
            instance = new TerminalDTO();
        }
        Page<Terminal> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.terminalMapper.selectObjByConditionQuery(instance);
        return page;
    }

    @Override
    public List<Terminal> selectObjByMap(Map params) {
        return this.terminalMapper.selectObjByMap(params);
    }

    @Override
    public List<Terminal> selectObjToProbe(Map params) {
        return this.terminalMapper.selectObjToProbe(params);
    }

    @Override
    public List<Terminal> selectObjHistoryByMap(Map params) {
        return this.terminalMapper.selectObjHistoryByMap(params);
    }

    @Override
    public List<Terminal> selectPartitionTerminal(Map params) {
        return this.terminalMapper.selectPartitionTerminal(params);
    }

    @Override
    public List<Terminal> selectPartitionTerminalHistory(Map params) {
        return this.terminalMapper.selectPartitionTerminalHistory(params);
    }

    @Override
    public List<Terminal> selectDeviceIpByNSwitch() {
        return this.terminalMapper.selectDeviceIpByNSwitch();
    }

    @Override
    public List<Terminal> selectObjByNeIp() {
        return this.terminalMapper.selectObjByNeIp();
    }

    @Override
    public List<Terminal> selectVMHost() {
        return this.terminalMapper.selectVMHost();
    }

    @Override
    public List<Terminal> selectNSwitchToTopology(Map params) {
        return this.terminalMapper.selectNSwitchToTopology(params);
    }

    @Override
    public List<Terminal> selectHistoryNSwitchToTopology(Map params) {
        return this.terminalMapper.selectHistoryNSwitchToTopology(params);
    }

    @Override
    public boolean updateVMHostDeviceType() {
        try {
            this.terminalMapper.updateVMHostDeviceType();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateVMDeviceType() {
        try {
            this.terminalMapper.updateVMDeviceType();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean updateVMDeviceIp() {
        try {
            this.terminalMapper.updateVMDeviceIp();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateObjDeviceTypeByMac() {
        Map params = new HashMap();
        params.put("v4ipIsNull", "v4ipIsNull");
        params.put("deviceType", 0);
        List<Terminal> TerminalList = this.terminalMapper.selectObjByMap(params);
        if(!TerminalList.isEmpty()){
            for (Terminal terminal : TerminalList) {
                String vendor = VerifyMacVendorUtils.toDevice(terminal.getMacVendor());
                if(StringUtil.isNotEmpty(vendor)){
                    terminal.setDeviceType(1);
                    this.terminalMapper.update(terminal);
                }
            }

        }

        return false;
    }

    @Override
    public boolean save(Terminal instance) {
        if (instance.getId() == null) {
            if (instance.getId() == null || instance.getId().equals("")) {
                instance.setAddTime(new Date());
                instance.setFrom(1);
                instance.setTag("DT");
                instance.setUuid(UUID.randomUUID().toString());
            } else {
                if (instance.getFrom() != null && Strings.isBlank(instance.getFrom().toString())) {
                    instance.setFrom(1);
                }
            }
            try {
                this.terminalMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                if (instance.getType() == null || instance.getType().equals("")) {
                    instance.setType(1);
                } else {
                    if (instance.getType() != 1) {
                        instance.setType(1);
                    }
                }
                this.terminalMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean update(Terminal instance) {
        try {
            instance.setUpdateTime(new Date());
            this.terminalMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int delete(Long id) {
        return this.terminalMapper.delete(id);
    }

    @Override
    public boolean deleteObjByType(Integer type) {
        try {
            this.terminalMapper.deleteObjByType(type);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchSave(List<Terminal> instance) {
        try {
            this.terminalMapper.batchSave(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchUpdate(List<Terminal> instance) {
        try {
            this.terminalMapper.batchUpdate(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void syncTerminal(Date date) {
        DeviceType deviceType1 = this.deviceTypeService.selectObjByType(14);
        DeviceType deviceType2 = this.deviceTypeService.selectObjByType(27);

        // 求交集，更新为终端在线
        List<Terminal> inner = this.terminalMapper.selectObjIntersection();
        if (inner.size() > 0) {
            inner.stream().forEach(e -> {
                e.setOnline(true);
                this.setDevice(e, date, deviceType1, deviceType2);
            });
        }

        // 求差集
        try {
            List<Terminal> left = this.terminalMapper.selectObjLeftdifference();
            if (left.size() > 0) {
                left.stream().forEach(e -> {
                    e.setAddTime(date);
                    e.setOnline(true);
                    e.setType(0);
                    e.setUuid(UUID.randomUUID().toString());
                    this.setDevice(e, date, deviceType1, deviceType2);

                });
            }
            // 批量插入
            if (left.size() > 0) {
                this.terminalMapper.batchSave(left);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        List<Terminal> right = this.terminalMapper.selectObjRightdifference();
        if (right.size() > 0) {
            right.stream().forEach(e -> {
                e.setOnline(false);
                this.setDevice(e, date, deviceType1, deviceType2);
            });
            inner.addAll(right);
        }

        // 批量更新
        if (inner != null && !inner.isEmpty()) {
//            for (Terminal terminal : inner) {
//                this.terminalMapper.update(terminal);
//            }
            this.terminalMapper.batchUpdate(inner);
        }

        List<Terminal> neTerminalList = this.terminalMapper.selectObjByNeIp();
        if(!neTerminalList.isEmpty()){
            for (Terminal terminal : neTerminalList) {
                try {
                    this.terminalMapper.update(terminal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

//        // 删除没有Ip地址的终端
//        List<Terminal> terminals = this.terminalMapper.selectV4ipIsNullAndV6ipIsNull();
//        if(terminals.size() > 0){
//            for (Terminal terminal : terminals) {
//                try {
//                    this.terminalMapper.delete(terminal.getId());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }




    }

    // 已存在更新
//    @Override
//    public void v4Tov6Terminal(Date date) {
//
//        DeviceType deviceType1 = this.deviceTypeService.selectObjByType(14);
//        DeviceType deviceType2 = this.deviceTypeService.selectObjByType(27);
//
//        List<Terminal> terminals = this.terminalMapper.selectObjByMap(null);
//        if(terminals.size() > 0){
//            for (Terminal terminal : terminals) {
//                this.terminalMapper.delete(terminal.getId());
//            }
//        }
//        List<Arp> arps = this.arpService.selectObjDistinctV4ip();
//        if(arps.size() > 0){
//            for (Arp arp : arps) {
//                Terminal terminal = new Terminal();
//                terminal.setAddTime(date);
//                terminal.setV4ip(arp.getV4ip());
//                terminal.setV6ip(arp.getV6ip());
//                terminal.setMac(arp.getMac());
//                terminal.setMacVendor(arp.getMacVendor());
//                terminal.setPort(arp.getPort());
//                terminal.setTag("DT");
//                terminal.setDeviceIp(arp.getDeviceIp());
//                terminal.setUuid(UUID.randomUUID().toString());
//                terminal.setOnline(true);
//                this.setDevice(terminal, date, deviceType1, deviceType2);
//                this.terminalMapper.save(terminal);
//            }
//        }
//    }

    @Override
    public void v4Tov6Terminal(Date date) {

        DeviceType deviceType1 = this.deviceTypeService.selectObjByType(14);
        DeviceType deviceType2 = this.deviceTypeService.selectObjByType(27);

        List<Arp> arps = this.arpService.selectObjDistinctV4ip();
        List<Terminal> arpTerminal = new ArrayList<>();
        if(arps.size() > 0){
            for (Arp arp : arps) {
                Terminal terminal = new Terminal();
                terminal.setAddTime(date);
                terminal.setV4ip(arp.getV4ip());
                terminal.setV6ip(arp.getV6ip());
                terminal.setMac(arp.getMac());
                terminal.setMacVendor(arp.getMacVendor());
                terminal.setPort(arp.getPort());
                terminal.setTag("DT");
                terminal.setDeviceIp(arp.getDeviceIp());
                terminal.setUuid(UUID.randomUUID().toString());
                terminal.setOnline(true);
                arpTerminal.add(terminal);
            }
        }

        List<Terminal> terminals = this.terminalMapper.selectObjByMap(null);
        if(arpTerminal.size() > 0){
            if(terminals.size() <= 0){
                for (Terminal terminal : arpTerminal) {
                    this.setDevice(terminal, date, deviceType1, deviceType2);
                    this.terminalMapper.save(terminal);
                }
            }else{
                List<Terminal> insertList = TerminalUtils.different(arpTerminal, terminals);
                if(insertList.size() > 0){
                    for (Terminal terminal : insertList) {
                        this.setDevice(terminal, date, deviceType1, deviceType2);
                        this.terminalMapper.save(terminal);
                    }
                }

                List<Terminal> updateList = TerminalUtils.different(terminals, arpTerminal);
                if(updateList.size() > 0){
                    for (Terminal terminal : updateList) {
                        if(terminal.getOnline()){
                            terminal.setOnline(false);
//                            this.setDevice(terminal, date, deviceType1, deviceType2);
                            this.terminalMapper.update(terminal);
                        }
                    }
                }

                List<Terminal> commonTerminal = TerminalUtils.common(terminals, arpTerminal);
                if(commonTerminal.size() > 0){
                    for (Terminal terminal : commonTerminal) {
                        if(!terminal.getOnline()){
                            terminal.setOnline(true);
                        }
                        this.setDevice(terminal, date, deviceType1, deviceType2);
                        this.terminalMapper.update(terminal);
                    }
                }
            }
        }else{
            if(terminals.size() > 0){
                // 排除相同mac地址arp，并判断type是否为修改过终端类型的终端
                for (Terminal terminal : terminals) {
                    terminal.setOnline(false);
                    this.terminalMapper.update(terminal);
                }
            }
        }

    }

    @Override
    public void writeTerminalUnit() {

        List<TerminalUnitSubnet> ipv4Subnet = terminalUnitSubnetService.selectObjByMap(null);

        List<TerminalUnit> terminalUnitList = this.terminalUnitService.selectObjByMap(null);

        if(ipv4Subnet.size() > 0 && terminalUnitList.size() > 0){

            Map<Long, Long> longStringMap = new HashMap<>();

            for (TerminalUnit terminalUnit : terminalUnitList) {
                longStringMap.put(terminalUnit.getId(), terminalUnit.getId());
            }

            Map<String, Long> map = new HashMap<>();

            for (TerminalUnitSubnet terminalUnitSubnet : ipv4Subnet) {
                if(terminalUnitSubnet.getIp() != null
                        && terminalUnitSubnet.getMask() != null
                        && terminalUnitSubnet.getTerminalUnitId() != null){
                    if(longStringMap.get(terminalUnitSubnet.getTerminalUnitId()) != null){
                        map.put(terminalUnitSubnet.getIp() + "/"
                                + terminalUnitSubnet.getMask(), longStringMap.get(terminalUnitSubnet.getTerminalUnitId()));
                    }
                }
            }

            Map params = new HashMap();
            params.put("online", false);
            List<Terminal> onlineTerminal = this.terminalMapper.selectObjByMap(params);
            if(onlineTerminal.size() > 0){
                for (Terminal terminal : onlineTerminal) {
                    if(StringUtil.isNotEmpty(terminal.getV4ip())){
                        terminal.setUnitId(null);
                        this.terminalMapper.update(terminal);
                    }
                }
            }

            if(map.size() > 0){
                List<Terminal> terminalList = this.terminalMapper.selectObjByMap(null);
                if(terminalList.size() > 0){
                    for (Terminal terminal : terminalList) {
                        String ip = terminal.getV4ip();
                        if(StringUtil.isEmpty(ip)){
                            continue;
                        }
                        try {
                            Long result = IpSubnetMap.findSubnetForIp(map, ip);
                            if(result != null){
                                if(terminal.getUnitId() == null ||
                                        !"".equals(terminal.getUnitId())){
                                    TerminalUnit terminalUnit = this.terminalUnitService.selectObjById(result);
                                    if(terminalUnit != null){
                                        terminal.setUnitId(result);
                                        terminal.setUnitName(terminalUnit.getName());
                                    }else{
                                        terminal.setUnitId(null);
                                        terminal.setUnitName(null);
                                    }
                                    this.terminalMapper.update(terminal);
                                }
                            }else{
                                terminal.setUnitId(null);
                                terminal.setUnitName(null);
                                this.terminalMapper.update(terminal);
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void writeTerminalUnitV6() {

        List<TerminalUnitSubnetV6> ipv6Subnet = terminalUnitSubnetV6Service.selectObjByMap(null);

        List<TerminalUnit> terminalUnitList = this.terminalUnitService.selectObjByMap(null);

        if(ipv6Subnet.size() > 0 && terminalUnitList.size() > 0){

            Map<Long, Long> longStringMap = new HashMap<>();

            for (TerminalUnit terminalUnit : terminalUnitList) {
                longStringMap.put(terminalUnit.getId(), terminalUnit.getId());
            }

            Map<String, Long> map = new HashMap<>();

            for (TerminalUnitSubnetV6 terminalUnitSubnet : ipv6Subnet) {
                if(terminalUnitSubnet.getIp() != null
                        && terminalUnitSubnet.getMask() != null
                        && terminalUnitSubnet.getTerminalUnitId() != null){
                    if(longStringMap.get(terminalUnitSubnet.getTerminalUnitId()) != null){
                        map.put(terminalUnitSubnet.getIp() + "/"
                                + terminalUnitSubnet.getMask(), longStringMap.get(terminalUnitSubnet.getTerminalUnitId()));
                    }
                }
            }

            Map params = new HashMap();
            params.put("online", false);
            List<Terminal> onlineTerminal = this.terminalMapper.selectObjByMap(params);
            if(onlineTerminal.size() > 0){
                for (Terminal terminal : onlineTerminal) {
                    if(StringUtil.isEmpty(terminal.getV4ip()) && StringUtil.isNotEmpty(terminal.getV6ip())){
                        terminal.setUnitId(null);
                        this.terminalMapper.update(terminal);
                    }
                }
            }

            if(map.size() > 0){
                List<Terminal> terminalList = this.terminalMapper.selectObjByMap(null);
                if(terminalList.size() > 0){
                    for (Terminal terminal : terminalList) {
                        String ip = terminal.getV6ip();
                        if(StringUtil.isEmpty(terminal.getV4ip()) && StringUtil.isNotEmpty(terminal.getV6ip())){
                            try {
                                Long result = Ipv6SubnetMap.findSubnetForIp(map, ip);
                                if(result != null){
                                    if(terminal.getUnitId() == null ||
                                            !"".equals(terminal.getUnitId())){
                                        TerminalUnit terminalUnit = this.terminalUnitService.selectObjById(result);
                                        if(terminalUnit != null){
                                            terminal.setUnitId(result);
                                            terminal.setUnitName(terminalUnit.getName());
                                        }else{
                                            terminal.setUnitId(null);
                                            terminal.setUnitName(null);
                                        }
                                        this.terminalMapper.update(terminal);
                                    }
                                }else{
                                    terminal.setUnitId(null);
                                    terminal.setUnitName(null);
                                    this.terminalMapper.update(terminal);
                                }
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
        }
    }

    @Override
    public void dualStackTerminal() {
        List<Terminal> terminalList = this.terminalMapper.selectObjByMap(null);
        if(terminalList.size() > 0){
            for (Terminal terminal : terminalList) {
                if(StringUtil.isNotEmpty(terminal.getV6ip())){
                    TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Mapper.getMacByMacAddress(terminal.getMac());
                    if(terminalMacIpv6 == null){
                        this.terminalMacIpv6Mapper.insertMac(terminal.getMac(), 1);
                    }
                }
            }
        }
    }

    @Override
    public void writeTerminalType() {
        List<Terminal> terminalList = this.terminalMapper.selectObjByMap(null);
        if(terminalList.size() > 0){
            Map params = new HashMap();
            for (Terminal terminal : terminalList) {
                if(StringUtil.isNotEmpty(terminal.getMac())){
                    // 通过mac查找vendor
                    if(StringUtil.isNotEmpty(terminal.getMac())){
                        String mac = MacUtils.getMac(terminal.getMac());
                        params.clear();
                        params.put("mac", mac);
                        List<MacVendor> macVendors = this.macVendorMapper.selectObjByMap(params);
                        if(macVendors.size() > 0){
                            MacVendor macVendor = macVendors.get(0);
                            // 查询设备类型
                            if(StringUtils.isNotEmpty(macVendor.getVendor())){
                                // 查询termial_mac_vendor 写入设备类型
                                TerminalMacVendor terminalMacVendor = this.terminalMacVendorMapper.selectByVendor(macVendor.getVendor());
                                if(terminalMacVendor != null){
                                    if(terminal.getType() == null || terminal.getType() != 1){
                                        DeviceType deviceType = this.deviceTypeService.selectObjById(terminalMacVendor.getTerminalTypeId());
                                        if(deviceType != null){
                                            terminal.setDeviceTypeId(deviceType.getId());
                                            this.terminalMapper.update(terminal);
                                        }
                                    }
                                }
                            }
                        }

                    }

                }
            }
        }
    }

    @Override
    public void writeTerminalDeviceTypeToVendor() {
        String vendor = "VMware, Inc.";
        Map params = new HashMap();
        params.put("macVendor", vendor);
        List<Terminal> terminals = this.terminalMapper.selectObjByMap(params);
        if(!terminals.isEmpty()){
            terminals.forEach(e -> {
                try {
                    e.setDeviceTypeId(9L);
                    this.terminalMapper.update(e);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        }
    }

    public void setDevice(Terminal e, Date date, DeviceType type1, DeviceType type2) {
        e.setTime(date);
        Map params = new HashMap();
        if (StringUtils.isNotEmpty(e.getDeviceIp())) {
            params.clear();
            params.put("ip", e.getDeviceIp());
            List<NetworkElement> nes = this.networkElementService.selectObjByMap(params);
            if (nes.size() > 0) {
                NetworkElement ne = nes.get(0);
                e.setDeviceUuid(ne.getUuid());
            }
        } else {
            // nswtich
            params.clear();
            params.put("deviceName", e.getDeviceName());
            params.put("deleteStatus", 1);
            List<NetworkElement> NSwitch_nes = this.networkElementService.selectObjByMap(params);
            if (NSwitch_nes.size() > 0) {
                NetworkElement ne = NSwitch_nes.get(0);
                e.setDeviceUuid(ne.getUuid());
                e.setDeviceIp(ne.getIp());

            } else {
                // 写入ap设备信息
                // ...
            }
        }
        // 写入ap设备信息
        if (StringUtils.isNotBlank(e.getDeviceName())) {
            params.clear();
            params.put("deviceName", e.getDeviceName());
            params.put("type", 3);
            List<NetworkElement> ap_nes = this.networkElementService.selectObjByMap(params);
            if (ap_nes.size() > 0) {
                NetworkElement ne = ap_nes.get(0);
                e.setDeviceUuid(ne.getUuid());
                e.setDeviceIp(ne.getIp());
                e.setDeviceName(ne.getDeviceName());
            }
        }

        if (StringUtils.isNotEmpty(e.getRemoteDeviceIp())) {
            params.clear();
            params.put("ip", e.getRemoteDeviceIp());
            List<NetworkElement> nes2 = this.networkElementService.selectObjByMap(params);
            if (nes2.size() > 0) {
                NetworkElement ne = nes2.get(0);
                e.setRemoteDeviceUuid(ne.getUuid());
            }
        }
        // 判断设备在线/离线、设备uuid/port、根据端口up/down
        this.verifyTerminalOnlineOfOffline(e.getDeviceUuid(), e.getPort(), e);

        if (e.getOnline()) {
            if (e.getType() == null || e.getType() == 0) {
                if (StringUtils.isEmpty(e.getV4ip()) && StringUtils.isEmpty(e.getV6ip())) {
                    e.setDeviceTypeId(type2.getId());
                } else if (StringUtils.isNotEmpty(e.getV4ip()) || StringUtils.isNotEmpty(e.getV6ip())) {
                    e.setDeviceTypeId(type1.getId());
                } else {
                    e.setDeviceTypeId(type1.getId());
                }
            }
        }
    }

    public void verifyTerminalOnlineOfOffline(String deviceUuid, String port, Terminal terminal) {
        Map params = new HashMap();
        if (StringUtils.isNotBlank(deviceUuid) && StringUtils.isNotBlank(port)) {
            params.put("port", port);
            params.put("deviceUuid", deviceUuid);
            List<Port> ports = this.portService.selectObjByMap(params);
            if (ports.size() > 0) {
                Port port1 = ports.get(0);
                if(port1 != null){
                    if(port1.getStatus().equals(1)){
                        terminal.setOnline(true);
                    }
                    if(port1.getStatus().equals(2)){
                        terminal.setOnline(false);
                    }
                }
            }
//            List<PortIpv6> portIpv6s = this.portIpv6Service.selectObjByMap(params);
//            if (portIpv6s.size() > 0) {
//                PortIpv6 portIpv6 = ports.get(0);
//                if(portIpv6 != null){
//                    if(portIpv6.getStatus().equals(1)){
//
//                    }
//                    if(portIpv6.getStatus().equals(2)){
//
//                    }
//                }
//            }
        }
    }

    // 优化：对比上次采集结果，如果没有变化则不记录
    @Override
    public void syncTerminalToTerminalHistory() {
        this.terminalMapper.copyTerminalToTerminalHistory();
    }

    @Override
    public Map<String, Integer> terminalCount() {
        return this.terminalMapper.terminalCount();
    }

    ;
}

