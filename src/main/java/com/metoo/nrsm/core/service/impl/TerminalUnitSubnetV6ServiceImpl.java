package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.TerminalUnitSubnetMapper;
import com.metoo.nrsm.core.mapper.TerminalUnitSubnetV6Mapper;
import com.metoo.nrsm.core.service.ITerminalUnitSubnetService;
import com.metoo.nrsm.core.service.ITerminalUnitSubnetV6Service;
import com.metoo.nrsm.entity.TerminalUnitSubnet;
import com.metoo.nrsm.entity.TerminalUnitSubnetV6;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TerminalUnitSubnetV6ServiceImpl implements ITerminalUnitSubnetV6Service {

    @Resource
    private TerminalUnitSubnetV6Mapper terminalUnitSubnetV6Mapper;

    @Override
    public TerminalUnitSubnetV6 selectObjById(Long id) {
        return this.terminalUnitSubnetV6Mapper.selectObjById(id);
    }

    @Override
    public List<TerminalUnitSubnetV6> selectObjByMap(Map params) {
        return this.terminalUnitSubnetV6Mapper.selectObjByMap(params);
    }

}
