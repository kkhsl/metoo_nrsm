package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.core.mapper.TerminalMapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<Terminal> selectObjHistoryByMap(Map params) {
        return this.terminalMapper.selectObjHistoryByMap(params);
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

        // 批量更新
        // 求差集，terminal更新终端为不在线，mac表DT插入terminal
        try {
            List<Terminal> left = this.terminalMapper.selectObjLeftdifference();
            if (left.size() > 0) {
                left.stream().forEach(e -> {
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
        if (inner.size() > 0) {
            this.terminalMapper.batchUpdate(inner);
        }
    }

    public void setDevice(Terminal e, Date date, DeviceType type1, DeviceType type2) {
        Map params = new HashMap();

        if (StringUtils.isNotEmpty(e.getDeviceIp())) {
            params.clear();
            params.put("ip", e.getDeviceIp());
            List<NetworkElement> nes = this.networkElementService.selectObjByMap(params);
            if (nes.size() > 0) {
                NetworkElement ne = nes.get(0);
                e.setDeviceUuid(ne.getUuid());
                e.setAddTime(date);
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
                e.setAddTime(date);
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
                e.setAddTime(date);
            }
        }

        if (StringUtils.isNotEmpty(e.getRemoteDeviceIp())) {
            params.clear();
            params.put("ip", e.getRemoteDeviceIp());
            List<NetworkElement> nes2 = this.networkElementService.selectObjByMap(params);
            if (nes2.size() > 0) {
                NetworkElement ne = nes2.get(0);
                e.setRemoteDeviceUuid(ne.getUuid());
                e.setAddTime(date);
            }
        }
        // 判断设备在线/离线、设备uuid/port、根据端口up/down
        boolean onlineFlag = this.verifyTerminalOnlineOfOffline(e.getDeviceUuid(), e.getPort());
        e.setOnline(onlineFlag);
        if (e.getOnline()) {
            if (e.getType() == null || e.getType() == 0) {
                if (StringUtils.isEmpty(e.getV4ip()) && StringUtils.isEmpty(e.getV6ip())) {
                    e.setDeviceTypeId(type2.getId());
                } else {
                    e.setDeviceTypeId(type1.getId());
                }
            }
        }
    }

    public boolean verifyTerminalOnlineOfOffline(String deviceUuid, String port) {
        Map params = new HashMap();
        if (StringUtils.isNotBlank(deviceUuid) && StringUtils.isNotBlank(port)) {
            params.put("port", port);
            params.put("deviceUuid", deviceUuid);
            List<Port> ports = this.portService.selectObjByMap(params);
            if (ports.size() > 0) {
                return true;
            }
            List<PortIpv6> portIpv6s = this.portIpv6Service.selectObjByMap(params);
            if (portIpv6s.size() > 0) {
                return true;
            }
        }
        return false;
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

