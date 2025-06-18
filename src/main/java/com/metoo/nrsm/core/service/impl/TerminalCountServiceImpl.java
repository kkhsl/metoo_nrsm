package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.core.mapper.TerminalCountMapper;
import com.metoo.nrsm.core.mapper.TerminalMapper;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.ITerminalCountService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalCount;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TerminalCountServiceImpl implements ITerminalCountService {

    @Autowired
    private TerminalCountMapper terminalCountMapper;

    @Override
    public TerminalCount selectObjByMap(Map params) {
        return this.terminalCountMapper.selectObjByMap(params);
    }

    @Override
    public TerminalCount selectHistoryObjByMap(Map params) {
        return this.terminalCountMapper.selectHistoryObjByMap(params);
    }

    @Override
    public boolean save(TerminalCount instance) {
        if(instance.getId() == null){
            try {
                this.terminalCountMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }else{
            try {
                this.terminalCountMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}

