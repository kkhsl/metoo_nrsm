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
import com.metoo.nrsm.core.utils.ip.Ipv6.IPv6SubnetCheck;
import com.metoo.nrsm.core.utils.ip.ipv4.IpSubnetMap;
import com.metoo.nrsm.core.utils.ip.ipv4.Ipv6SubnetMap;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    @Autowired
    private IMacVendorDeviceTypeService macVendorDeviceTypeService;
    @Autowired
    private IUnitSubnetService unitSubnetService;

    @Override
    public Terminal selectObjById(Long id) {
        return terminalMapper.selectObjById(id);
    }

    @Override
    public Page<Terminal> selectObjByConditionQuery(TerminalDTO instance) {
        if (instance == null) {
            instance = new TerminalDTO();
        }
        Page<Terminal> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        terminalMapper.selectObjByConditionQuery(instance);
        return page;
    }

    @Override
    public List<Terminal> selectObjByMap(Map params) {
        return terminalMapper.selectObjByMap(params);
    }

    @Override
    public List<Terminal> selectObjToProbe(Map params) {
        return terminalMapper.selectObjToProbe(params);
    }

    @Override
    public List<Terminal> selectObjHistoryByMap(Map params) {
        return terminalMapper.selectObjHistoryByMap(params);
    }

    @Override
    public List<Terminal> selectPartitionTerminal(Map params) {
        return terminalMapper.selectPartitionTerminal(params);
    }

    @Override
    public List<Terminal> selectPartitionTerminalHistory(Map params) {
        return terminalMapper.selectPartitionTerminalHistory(params);
    }

    @Override
    public List<Terminal> selectDeviceIpByNSwitch() {
        return terminalMapper.selectDeviceIpByNSwitch();
    }

    @Override
    public List<Terminal> selectObjByNeIp() {
        return terminalMapper.selectObjByNeIp();
    }

    @Override
    public List<Terminal> selectVMHost() {
        return terminalMapper.selectVMHost();
    }

    @Override
    public List<Terminal> selectNSwitchToTopology(Map params) {
        return terminalMapper.selectNSwitchToTopology(params);
    }

    @Override
    public List<Terminal> selectHistoryNSwitchToTopology(Map params) {
        return terminalMapper.selectHistoryNSwitchToTopology(params);
    }

    @Override
    public List<Terminal> selectCustomPartitionByMap(Map params) {
        return terminalMapper.selectCustomPartitionByMap(params);
    }

    @Override
    public List<Terminal> selectCustomPartitionHistoryByMap(Map params) {
        return terminalMapper.selectCustomPartitionHistoryByMap(params);
    }

    @Override
    public boolean updateVMHostDeviceType() {
        try {
            terminalMapper.updateVMHostDeviceType();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateVMDeviceType() {
        try {
            terminalMapper.updateVMDeviceType();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean updateVMDeviceIp() {
        try {
            terminalMapper.updateVMDeviceIp();
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
        List<Terminal> TerminalList = terminalMapper.selectObjByMap(params);
        if (!TerminalList.isEmpty()) {
            for (Terminal terminal : TerminalList) {
                String vendor = VerifyMacVendorUtils.toDevice(terminal.getMacVendor());
                if (StringUtil.isNotEmpty(vendor)) {
                    terminal.setDeviceType(1);
                    terminalMapper.update(terminal);
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
                terminalMapper.save(instance);
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
                terminalMapper.update(instance);
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
            terminalMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int delete(Long id) {
        return terminalMapper.delete(id);
    }

    @Override
    public boolean deleteObjByType(Integer type) {
        try {
            terminalMapper.deleteObjByType(type);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchSave(List<Terminal> instance) {
        try {
            terminalMapper.batchSave(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchUpdate(List<Terminal> instance) {
        try {
            terminalMapper.batchUpdate(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void syncTerminal(Date date) {
        DeviceType deviceTypeDesktop = deviceTypeService.selectObjByType(14);// 台式电脑
        DeviceType deviceTypeOther = deviceTypeService.selectObjByType(27);// 其他

        // 求交集，更新为终端在线
        List<Terminal> inner = terminalMapper.selectObjIntersection();
        if (inner.size() > 0) {
            inner.stream().forEach(e -> {
                e.setOnline(true);
                setDeviceToTerminal(e, date, deviceTypeDesktop, deviceTypeOther);
            });
        }

        // 求差集
        try {
            List<Terminal> left = terminalMapper.selectObjLeftdifference();
            if (left.size() > 0) {
                left.stream().forEach(e -> {
                    e.setAddTime(date);
                    e.setOnline(true);
                    e.setType(0);
                    e.setUuid(UUID.randomUUID().toString());
                    setDeviceToTerminal(e, date, deviceTypeDesktop, deviceTypeOther);

                });
            }
            // 批量插入
            if (left.size() > 0) {
                terminalMapper.batchSave(left);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        List<Terminal> right = terminalMapper.selectObjRightdifference();
        if (right.size() > 0) {
            right.stream().forEach(e -> {
                e.setOnline(false);
                setDeviceToTerminal(e, date, deviceTypeDesktop, deviceTypeOther);
            });
            inner.addAll(right);
        }

        // 批量更新
        if (inner != null && !inner.isEmpty()) {
//            for (Terminal terminal : inner) {
//                terminalMapper.update(terminal);
//            }
            terminalMapper.batchUpdate(inner);
        }

        List<Terminal> neTerminalList = terminalMapper.selectObjByNeIp();
        if (!neTerminalList.isEmpty()) {
            for (Terminal terminal : neTerminalList) {
                try {
                    terminalMapper.update(terminal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

//        // 删除没有Ip地址的终端
//        List<Terminal> terminals = terminalMapper.selectV4ipIsNullAndV6ipIsNull();
//        if(terminals.size() > 0){
//            for (Terminal terminal : terminals) {
//                try {
//                    terminalMapper.delete(terminal.getId());
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
//        DeviceType deviceType1 = deviceTypeService.selectObjByType(14);
//        DeviceType deviceType2 = deviceTypeService.selectObjByType(27);
//
//        List<Terminal> terminals = terminalMapper.selectObjByMap(null);
//        if(terminals.size() > 0){
//            for (Terminal terminal : terminals) {
//                terminalMapper.delete(terminal.getId());
//            }
//        }
//        List<Arp> arps = arpService.selectObjDistinctV4ip();
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
//                setDevice(terminal, date, deviceType1, deviceType2);
//                terminalMapper.save(terminal);
//            }
//        }
//    }

    @Override
    public void v4Tov6Terminal(Date date) {

        DeviceType desktop = deviceTypeService.selectObjByType(14);
        DeviceType other = deviceTypeService.selectObjByType(27);

        List<Arp> arps = arpService.selectObjDistinctV4ip();
        List<Terminal> arpTerminal = new ArrayList<>();
        if (arps.size() > 0) {
            for (Arp arp : arps) {
                Terminal terminal = new Terminal();
                terminal.setAddTime(date);
                terminal.setTime(date);
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

        List<Terminal> terminals = terminalMapper.selectObjByMap(null);
        if (arpTerminal.size() > 0) {
            if (terminals.size() <= 0) {
                for (Terminal terminal : arpTerminal) {
                    setDeviceToTerminal(terminal, date, desktop, other);
                    terminalMapper.save(terminal);
                }
            } else {
                List<Terminal> insertList = TerminalUtils.different(arpTerminal, terminals);
                if (insertList.size() > 0) {
                    for (Terminal terminal : insertList) {
                        setDeviceToTerminal(terminal, date, desktop, other);
                        terminalMapper.save(terminal);
                    }
                }

                List<Terminal> updateList = TerminalUtils.different(terminals, arpTerminal);
                if (updateList.size() > 0) {
                    for (Terminal terminal : updateList) {
                        if (terminal.getOnline()) {
                            terminal.setOnline(false);
//                            setDevice(terminal, date, deviceType1, deviceType2);
                            terminalMapper.update(terminal);
                        }
                    }
                }

                List<Terminal> commonTerminal = TerminalUtils.common(terminals, arpTerminal);
                if (commonTerminal.size() > 0) {
                    for (Terminal terminal : commonTerminal) {
                        if (!terminal.getOnline()) {
                            terminal.setOnline(true);
                        }
                        setDeviceToTerminal(terminal, date, desktop, other);
                        terminalMapper.update(terminal);
                    }
                }
            }
        } else {
            if (terminals.size() > 0) {
                // 排除相同mac地址arp，并判断type是否为修改过终端类型的终端
                for (Terminal terminal : terminals) {
                    terminal.setOnline(false);
                    terminalMapper.update(terminal);
                }
            }
        }

    }

    @Override
    public void writeTerminalUnit() {

        List<TerminalUnitSubnet> ipv4Subnet = terminalUnitSubnetService.selectObjByMap(null);

        List<TerminalUnit> terminalUnitList = terminalUnitService.selectObjByMap(null);

        if (ipv4Subnet.size() > 0 && terminalUnitList.size() > 0) {

            Map<Long, Long> longStringMap = new HashMap<>();

            for (TerminalUnit terminalUnit : terminalUnitList) {
                longStringMap.put(terminalUnit.getId(), terminalUnit.getId());
            }

            Map<String, Long> map = new HashMap<>();

            for (TerminalUnitSubnet terminalUnitSubnet : ipv4Subnet) {
                if (terminalUnitSubnet.getIp() != null
                        && terminalUnitSubnet.getMask() != null
                        && terminalUnitSubnet.getTerminalUnitId() != null) {
                    if (longStringMap.get(terminalUnitSubnet.getTerminalUnitId()) != null) {
                        map.put(terminalUnitSubnet.getIp() + "/"
                                + terminalUnitSubnet.getMask(), longStringMap.get(terminalUnitSubnet.getTerminalUnitId()));
                    }
                }
            }

            Map params = new HashMap();
            params.put("online", false);
            List<Terminal> onlineTerminal = terminalMapper.selectObjByMap(params);
            if (onlineTerminal.size() > 0) {
                for (Terminal terminal : onlineTerminal) {
                    if (StringUtil.isNotEmpty(terminal.getV4ip())) {
                        terminal.setUnitId(null);
                        terminalMapper.update(terminal);
                    }
                }
            }

            if (map.size() > 0) {
                List<Terminal> terminalList = terminalMapper.selectObjByMap(null);
                if (terminalList.size() > 0) {
                    for (Terminal terminal : terminalList) {
                        String ip = terminal.getV4ip();
                        if (StringUtil.isEmpty(ip)) {
                            continue;
                        }
                        try {
                            Long result = IpSubnetMap.findSubnetForIp(map, ip);
                            if (result != null) {
                                if (terminal.getUnitId() == null ||
                                        !"".equals(terminal.getUnitId())) {
                                    TerminalUnit terminalUnit = terminalUnitService.selectObjById(result);
                                    if (terminalUnit != null) {
                                        terminal.setUnitId(result);
                                        terminal.setUnitName(terminalUnit.getName());
                                    } else {
                                        terminal.setUnitId(null);
                                        terminal.setUnitName(null);
                                    }
                                    terminalMapper.update(terminal);
                                }
                            } else {
                                terminal.setUnitId(null);
                                terminal.setUnitName(null);
                                terminalMapper.update(terminal);
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

        List<TerminalUnit> terminalUnitList = terminalUnitService.selectObjByMap(null);

        if (ipv6Subnet.size() > 0 && terminalUnitList.size() > 0) {

            Map<Long, Long> longStringMap = new HashMap<>();

            for (TerminalUnit terminalUnit : terminalUnitList) {
                longStringMap.put(terminalUnit.getId(), terminalUnit.getId());
            }

            Map<String, Long> map = new HashMap<>();

            for (TerminalUnitSubnetV6 terminalUnitSubnet : ipv6Subnet) {
                if (terminalUnitSubnet.getIp() != null
                        && terminalUnitSubnet.getMask() != null
                        && terminalUnitSubnet.getTerminalUnitId() != null) {
                    if (longStringMap.get(terminalUnitSubnet.getTerminalUnitId()) != null) {
                        map.put(terminalUnitSubnet.getIp() + "/"
                                + terminalUnitSubnet.getMask(), longStringMap.get(terminalUnitSubnet.getTerminalUnitId()));
                    }
                }
            }

            Map params = new HashMap();
            params.put("online", false);
            List<Terminal> onlineTerminal = terminalMapper.selectObjByMap(params);
            if (onlineTerminal.size() > 0) {
                for (Terminal terminal : onlineTerminal) {
                    if (StringUtil.isEmpty(terminal.getV4ip()) && StringUtil.isNotEmpty(terminal.getV6ip())) {
                        terminal.setUnitId(null);
                        terminalMapper.update(terminal);
                    }
                }
            }

            if (map.size() > 0) {
                List<Terminal> terminalList = terminalMapper.selectObjByMap(null);
                if (terminalList.size() > 0) {
                    for (Terminal terminal : terminalList) {
                        String ip = terminal.getV6ip();
                        if (StringUtil.isEmpty(terminal.getV4ip()) && StringUtil.isNotEmpty(terminal.getV6ip())) {
                            try {
                                Long result = Ipv6SubnetMap.findSubnetForIp(map, ip);
                                if (result != null) {
                                    if (terminal.getUnitId() == null ||
                                            !"".equals(terminal.getUnitId())) {
                                        TerminalUnit terminalUnit = terminalUnitService.selectObjById(result);
                                        if (terminalUnit != null) {
                                            terminal.setUnitId(result);
                                            terminal.setUnitName(terminalUnit.getName());
                                        } else {
                                            terminal.setUnitId(null);
                                            terminal.setUnitName(null);
                                        }
                                        terminalMapper.update(terminal);
                                    }
                                } else {
                                    terminal.setUnitId(null);
                                    terminal.setUnitName(null);
                                    terminalMapper.update(terminal);
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
    public void writeTerminalUnitByUnit2() {
        List<UnitSubnet> unitSubnetList = unitSubnetService.selectObjAll();
        Map params = new HashMap();
        params.put("online", false);
        List<Terminal> onlineTerminal = terminalMapper.selectObjByMap(params);
        if (onlineTerminal.size() > 0) {
            for (Terminal terminal : onlineTerminal) {
                if (terminal.getUnitId() != null) {
                    terminal.setUnitId(null);
                    terminalMapper.update(terminal);
                }
            }
        }

        List<Terminal> updatedTerminals = new ArrayList<>();
        if (!unitSubnetList.isEmpty()) {
            List<Terminal> terminalList = terminalMapper.selectObjByMap(null);
            if (!terminalList.isEmpty()) {
                outerLoop:
                for (Terminal terminal : terminalList) {
                    // 不排除，避免终端ip地址变化，导致写入错误网段
//                    if (StringUtils.isEmpty(terminal.getV4ip()) && StringUtils.isEmpty(terminal.getV6ip()) && terminal.getUnitId() != null) {
//                        continue;
//                    }// #TODO 记录以改的，未改的删除单位信息
                    if (StringUtils.isNotEmpty(terminal.getV4ip())) {
                        for (UnitSubnet unitSubnet : unitSubnetList) {
                            if (StringUtils.isNotEmpty(unitSubnet.getIpv4())) {
                                String[] array = unitSubnet.getIpv4().split(",");
                                for (String subnet : array) {
                                    try {
                                        boolean flag = IpSubnetMap.isIpInSubnet(terminal.getV4ip(), subnet);
                                        if (flag) {
                                            try {
                                                terminal.setUnitId(unitSubnet.getUnitId());
                                                terminalMapper.update(terminal);
                                                updatedTerminals.add(terminal);
                                                continue outerLoop; // 跳出外层循环
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } else if (StringUtils.isNotEmpty(terminal.getV6ip())) {
                        for (UnitSubnet unitSubnet : unitSubnetList) {
                            if (StringUtils.isNotEmpty(unitSubnet.getIpv6())) {
                                String[] array = unitSubnet.getIpv6().split(",");
                                for (String subnet : array) {
                                    boolean flag = IPv6SubnetCheck.isInSubnet(terminal.getV6ip(), subnet);
                                    if (flag) {
                                        terminal.setUnitId(unitSubnet.getUnitId());
                                        terminalMapper.update(terminal);
                                        updatedTerminals.add(terminal);
                                        continue outerLoop; // 跳出外层循环
                                    }
                                }
                            }
                        }
                    }
                }
                //
                // 获取未更新的终端：差集 = allTerminals - updatedTerminals
                List<Terminal> unchangedTerminals = terminalList.stream()
                        .filter(terminal -> !updatedTerminals.contains(terminal))
                        .collect(Collectors.toList());
                if (!unchangedTerminals.isEmpty()) {
                    for (Terminal unchangedTerminal : unchangedTerminals) {
                        try {
                            unchangedTerminal.setUnitId(null);
                            this.terminalMapper.update(unchangedTerminal);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }else{
            List<Terminal> terminalList = terminalMapper.selectObjByMap(null);
            if (!terminalList.isEmpty()) {
                for (Terminal terminal : terminalList) {
                    if(terminal.getUnitId() != null && !"".equals(terminal.getUnitId())){
                        try {
                            terminal.setUnitId(null);
                            this.terminalMapper.update(terminal);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    @Override
    public void dualStackTerminal() {
        List<Terminal> terminalList = terminalMapper.selectObjByMap(Collections.emptyMap());
        if (terminalList.isEmpty()) {
            return;
        }
        List<String> macList = new ArrayList<>();
        for (Terminal terminal : terminalList) {
            if ((StringUtil.isNotEmpty(terminal.getV6ip()) && !terminal.getV6ip().toLowerCase().startsWith("fe80"))
                    || StringUtil.isNotEmpty(terminal.getV6ip1()) && !terminal.getV6ip1().toLowerCase().startsWith("fe80")
                    || StringUtil.isNotEmpty(terminal.getV6ip2()) && !terminal.getV6ip2().toLowerCase().startsWith("fe80")
                    || StringUtil.isNotEmpty(terminal.getV6ip3()) && !terminal.getV6ip3().toLowerCase().startsWith("fe80")) {
                processTerminalMac(terminal, macList);
            }
        }
        if (!macList.isEmpty()) {
            List<TerminalMacIpv6> excludingTerminalMacIpv6s = terminalMacIpv6Mapper.findAllExcludingMacs(macList);
            if (!excludingTerminalMacIpv6s.isEmpty()) {
                excludingTerminalMacIpv6s.forEach(mac -> terminalMacIpv6Mapper.updateMac(mac.getMac(), 0));
            }
        } else {
            List<TerminalMacIpv6> excludingTerminalMacIpv6s = terminalMacIpv6Mapper.getAllMacs();
            if (!excludingTerminalMacIpv6s.isEmpty()) {
                excludingTerminalMacIpv6s.forEach(mac -> terminalMacIpv6Mapper.updateMac(mac.getMac(), 0));
            }
        }
    }

    private void processTerminalMac(Terminal terminal, List<String> macList) {
        TerminalMacIpv6 terminalMacIpv6 = terminalMacIpv6Mapper.getMacByMacAddress(terminal.getMac());
        if (terminalMacIpv6 == null) {
            try {
                terminalMacIpv6Mapper.insertMac(terminal.getMac(), 1);
                macList.add(terminal.getMac());
            } catch (Exception e) {
                log.error("Failed to insert MAC address: {}", terminal.getMac(), e);
            }
        } else {
            if (terminalMacIpv6.getIsIPv6() == 0) {
                terminalMacIpv6Mapper.updateMac(terminalMacIpv6.getMac(), 1);
            }
            macList.add(terminalMacIpv6.getMac());
        }
    }

    /*@Override
    public void writeTerminalType() {
        Map params = new HashMap();
        params.put("type", 0);
        List<Terminal> terminalList = terminalMapper.selectObjByMap(params);
        if(terminalList.size() > 0){
            for (Terminal terminal : terminalList) {
                if(StringUtil.isNotEmpty(terminal.getMac())){
                    // 通过mac查找vendor
                    if(StringUtil.isNotEmpty(terminal.getMac())){
                        String mac = MacUtils.getMac(terminal.getMac());
                        params.clear();
                        params.put("mac", mac);
                        List<MacVendor> macVendors = macVendorMapper.selectObjByMap(params);
                        if(macVendors.size() > 0){
                            MacVendor macVendor = macVendors.get(0);
                            // 查询设备类型
                            if(StringUtils.isNotEmpty(macVendor.getVendor())){
                                // 查询termial_mac_vendor 写入设备类型
                                TerminalMacVendor terminalMacVendor = terminalMacVendorMapper.selectByVendor(macVendor.getVendor());
                                if(terminalMacVendor != null){
                                    if(terminal.getType() == null || terminal.getType() != 1){
                                        DeviceType deviceType = deviceTypeService.selectObjById(terminalMacVendor.getTerminalTypeId());
                                        if(deviceType != null){
                                            terminal.setDeviceTypeId(deviceType.getId());
                                            terminalMapper.update(terminal);
                                        }
                                    }
                                }
                            }
                        }

                    }

                }
            }
        }
    }*/

    @Override
    public void writeTerminalType() {
        Map params = new HashMap();
        params.put("type", 0);
        List<Terminal> terminalList = terminalMapper.selectObjByMap(params);

        // 预加载设备类型避免多次查询
        DeviceType phoneType = deviceTypeService.selectObjByName("手机");
        DeviceType pcType = deviceTypeService.selectObjByName("台式电脑");

        for (Terminal terminal : terminalList) {
            String hostname = terminal.getClient_hostname();
            if (StringUtils.isNotEmpty(hostname)) {
                hostname = hostname.toUpperCase();  // 统一转换为大写方便匹配

                // 手机标识
                if (hostname.contains("HONOR") ||
                        hostname.contains("XIAOMI") ||
                        hostname.contains("HUAWEI") ||
                        hostname.contains("REDMI") ||
                        hostname.contains("IPHONE") ||
                        hostname.contains("OPPO") ||
                        hostname.contains("VIVO") ||
                        hostname.contains("ONEPLUS")) {

                    terminal.setDeviceTypeId(phoneType.getId());
                    terminalMapper.update(terminal);
                    continue;
                }

                // 电脑标识
                if (hostname.contains("DESKTOP-") ||
                        hostname.contains("PC-") ||
                        hostname.contains("WIN-") ||
                        hostname.contains("LAPTOP-")) {

                    terminal.setDeviceTypeId(pcType.getId());
                    terminalMapper.update(terminal);
                    continue;
                }
            }

            // 原始MAC地址处理逻辑（当主机名未匹配时执行）
            if (StringUtil.isNotEmpty(terminal.getMac())) {
                String mac = MacUtils.getMac(terminal.getMac());
                params.clear();
                params.put("mac", mac);
                List<MacVendor> macVendors = macVendorMapper.selectObjByMap(params);
                // 通过mac查找vendor
                if (!macVendors.isEmpty()) {
                    MacVendor macVendor = macVendors.get(0);
                    if (StringUtils.isNotEmpty(macVendor.getVendor())) {
                        TerminalMacVendor terminalMacVendor = terminalMacVendorMapper.selectByVendor(macVendor.getVendor());
                        if (terminalMacVendor != null &&
                                (terminal.getType() == null || terminal.getType() != 1)) {
                            // 查询设备类型
                            DeviceType deviceType = deviceTypeService.selectObjById(terminalMacVendor.getTerminalTypeId());
                            if (deviceType != null) {
                                terminal.setDeviceTypeId(deviceType.getId());
                                terminalMapper.update(terminal);
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
        List<Terminal> terminals = terminalMapper.selectObjByMap(params);
        if (!terminals.isEmpty()) {
            terminals.forEach(e -> {
                try {
                    e.setDeviceTypeId(9L);
                    terminalMapper.update(e);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        }
    }

    /**
     * 写入设备以及设备类型数据
     *
     * @param e
     * @param date
     * @param desktop 台式电脑
     * @param other   其他
     */
    public void setDeviceToTerminal(Terminal e, Date date, DeviceType desktop, DeviceType other) {
        e.setTime(date);
        e.setUpdateTime(date);
        Map params = new HashMap();
        if (StringUtils.isNotEmpty(e.getDeviceIp())) {
            params.clear();
            params.put("ip", e.getDeviceIp());
            List<NetworkElement> nes = networkElementService.selectObjByMap(params);
            if (nes.size() > 0) {
                NetworkElement ne = nes.get(0);
                e.setDeviceUuid(ne.getUuid());
            }
        } else {
            // nswtich
            params.clear();
            params.put("deviceName", e.getDeviceName());
            params.put("nswitch", 1);
            List<NetworkElement> NSwitch_nes = networkElementService.selectObjByMap(params);
            if (NSwitch_nes.size() > 0) {
                NetworkElement ne = NSwitch_nes.get(0);
                e.setDeviceUuid(ne.getUuid());
            } else {
                // 写入ap设备信息
                // ...
            }
            // 写入mac表时写入，这里不在查询
//            if(e.getDeviceIp2() != null && !"".equals(e.getDeviceIp2())){
//                params.clear();
//                params.put("ip", e.getDeviceIp2());
//                List<NetworkElement> oldNe = networkElementService.selectObjByMap(params);
//                if (oldNe.size() > 0) {
//                    NetworkElement ne = oldNe.get(0);
//                    e.setDeviceUuid2(ne.getUuid());
//                }
//            }

        }
        // 写入ap设备信息
        if (StringUtils.isNotBlank(e.getDeviceName())) {
            params.clear();
            params.put("deviceName", e.getDeviceName());
            params.put("type", 3);
            List<NetworkElement> ap_nes = networkElementService.selectObjByMap(params);
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
            List<NetworkElement> nes2 = networkElementService.selectObjByMap(params);
            if (nes2.size() > 0) {
                NetworkElement ne = nes2.get(0);
                e.setRemoteDeviceUuid(ne.getUuid());
            }
        }
        // 判断设备在线/离线、设备uuid/port、根据端口up/down
        verifyTerminalOnlineOfOffline(e.getDeviceUuid(), e.getPort(), e);

        if (e.getOnline()) {
//            if (e.getType() == null || e.getType() == 0) {
//                if (StringUtils.isEmpty(e.getV4ip()) && StringUtils.isEmpty(e.getV6ip())) {
////                    e.setDeviceTypeId(desktop.getId());
//                    // 如果类型未确定，根据mac地址比较
//                    Long deviceTypeId = getDeviceIdByMac(e.getMacVendor());
//                    if(deviceTypeId != null){
//                        e.setDeviceTypeId(deviceTypeId);
//                    }else{
//                        e.setDeviceTypeId(other.getId());
//                    }
//                } else if (StringUtils.isNotEmpty(e.getV4ip()) || StringUtils.isNotEmpty(e.getV6ip())) {
//                    e.setDeviceTypeId(desktop.getId());
//                } else {
//                    e.setDeviceTypeId(desktop.getId());
//                }
//            }
            if (e.getType() == null || e.getType() == 0) {
                // 如果类型未确定，根据mac地址比较
                Long deviceTypeId = getDeviceIdByMac(e.getMacVendor());
                if (deviceTypeId != null) {
                    e.setDeviceTypeId(deviceTypeId);
                } else {
                    if (StringUtils.isEmpty(e.getV4ip()) && StringUtils.isEmpty(e.getV6ip())) {
                        e.setDeviceTypeId(other.getId());
                    } else if (StringUtils.isNotEmpty(e.getV4ip()) || StringUtils.isNotEmpty(e.getV6ip())) {
                        e.setDeviceTypeId(desktop.getId());
                    } else {
                        e.setDeviceTypeId(desktop.getId());
                    }
                }
            }
        }
    }

    public Long getDeviceIdByMac(String macVendor) {
        // 查询设备类型
        MacVendorDeviceType macVendorDeviceType = macVendorDeviceTypeService.selectObjByMacVendor(macVendor);
        if (macVendorDeviceType != null && macVendorDeviceType.getDeviceTypeId() != null) {
            return macVendorDeviceType.getDeviceTypeId();
        }
        return null;
    }

    public void verifyTerminalOnlineOfOffline(String deviceUuid, String port, Terminal terminal) {
        Map params = new HashMap();
        if (StringUtils.isNotBlank(deviceUuid) && StringUtils.isNotBlank(port)) {
            params.put("port", port);
            params.put("deviceUuid", deviceUuid);
            List<Port> ports = portService.selectObjByMap(params);
            if (ports.size() > 0) {
                Port port1 = ports.get(0);
                if (port1 != null) {
                    if (port1.getStatus().equals(1)) {
                        terminal.setOnline(true);
                    }
                    if (port1.getStatus().equals(2)) {
                        terminal.setOnline(false);
                    }
                }
            }
//            List<PortIpv6> portIpv6s = portIpv6Service.selectObjByMap(params);
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
        terminalMapper.copyTerminalToTerminalHistory();
    }

    @Override
    public Map<String, Integer> terminalCount() {
        return terminalMapper.terminalCount();
    }

}

