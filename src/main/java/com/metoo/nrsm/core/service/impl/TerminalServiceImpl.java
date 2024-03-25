package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.core.mapper.TerminalMapper;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.Terminal;
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


    @Override
    public Terminal selectObjById(Long id) {
        return this.terminalMapper.selectObjById(id);
    }

    @Override
    public Page<Terminal> selectObjByConditionQuery(TerminalDTO instance) {
        if(instance == null){
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
        if(instance.getId() == null){
            if(instance.getId() == null || instance.getId().equals("")){
                instance.setAddTime(new Date());
                instance.setFrom(1);
                instance.setTag("DT");
                instance.setUuid(UUID.randomUUID().toString());
            }else{
                if(instance.getFrom() != null && Strings.isBlank(instance.getFrom().toString())){
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
        }else{
            try {
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
    public void syncTerminal(Date date){
        Map params = new HashMap();
        // 求交集，更新为终端在线
        List<Terminal> inner = this.terminalMapper.selectObjIntersection();
        if(inner.size() > 0){
            inner.stream().forEach(e->{
                e.setOnline(true);
                this.setDevice(e, date);
            });
        }

        // 批量更新
        // 求差集，terminal更新终端为不在线，mac表DT插入terminal
        try {
            List<Terminal> left = this.terminalMapper.selectObjLeftdifference();
            if(left.size() > 0){
                left.stream().forEach(e-> {
                    e.setOnline(true);
                    this.setDevice(e, date);
                    params.clear();
                    params.put("type", 14);
                    List<DeviceType> deviceTypes = this.deviceTypeService.selectObjByMap(params);
                    if(deviceTypes.size() > 0){
                        e.setDeviceTypeId(deviceTypes.get(0).getId());
                    }
                });
            }
            // 批量插入
            if(left.size() > 0){
                this.terminalMapper.batchSave(left);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Terminal> right = this.terminalMapper.selectObjRightdifference();
        if(right.size() > 0){
            right.stream().forEach(e->{
                e.setOnline(false);
                this.setDevice(e, date);
            });
            inner.addAll(right);
        }

        // 批量更新
        if(inner.size() > 0){
            this.terminalMapper.batchUpdate(inner);
        }
    }

    public void setDevice(Terminal e, Date date){
        Map params = new HashMap();
        if(StringUtils.isNotEmpty(e.getDeviceIp())){
            params.clear();
            params.put("ip", e.getDeviceIp());
            List<NetworkElement> nes = this.networkElementService.selectObjByMap(params);
            if(nes.size() > 0){
                NetworkElement ne = nes.get(0);
                e.setDeviceUuid(ne.getUuid());
                e.setAddTime(date);
            }
        }
        if(StringUtils.isNotEmpty(e.getRemoteDeviceIp())){
            params.clear();
            params.put("ip", e.getRemoteDeviceIp());
            List<NetworkElement> nes2 = this.networkElementService.selectObjByMap(params);
            if(nes2.size() > 0){
                NetworkElement ne = nes2.get(0);
                e.setRemoteDeviceUuid(ne.getUuid());
                e.setAddTime(date);
            }
        }

    }
    // 优化：对比上次采集结果，如果没有变化则不记录
    @Override
    public void syncTerminalToTerminalHistory() {
        this.terminalMapper.copyTerminalToTerminalHistory();
    } ;
}

