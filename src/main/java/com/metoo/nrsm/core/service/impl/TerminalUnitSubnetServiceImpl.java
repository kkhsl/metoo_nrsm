package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.mapper.TerminalUnitSubnetMapper;
import com.metoo.nrsm.core.service.ITerminalUnitService;
import com.metoo.nrsm.core.service.ITerminalUnitSubnetService;
import com.metoo.nrsm.entity.TerminalUnit;
import com.metoo.nrsm.entity.TerminalUnitSubnet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TerminalUnitSubnetServiceImpl implements ITerminalUnitSubnetService {

    @Resource
    private TerminalUnitSubnetMapper terminalUnitSubnetMapper;

    @Override
    public TerminalUnitSubnet selectObjById(Long id) {
        return this.terminalUnitSubnetMapper.selectObjById(id);
    }

    @Override
    public List<TerminalUnitSubnet> selectObjByMap(Map params) {
        return this.terminalUnitSubnetMapper.selectObjByMap(params);
    }
}
