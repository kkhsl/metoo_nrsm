package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.TerminalTypeMapper;
import com.metoo.nrsm.core.service.ITerminalTypeService;
import com.metoo.nrsm.entity.TerminalType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TerminalTypeServiceImpl implements ITerminalTypeService {


    @Autowired
    private TerminalTypeMapper terminalTypeMapper;

    @Override
    public TerminalType selectObjById(Long id) {
        return this.terminalTypeMapper.selectObjById(id);
    }

    @Override
    public TerminalType selectObjByType(Integer type) {
        return this.terminalTypeMapper.selectObjByType(type);
    }

    @Override
    public List<TerminalType> selectObjByMap(Map params) {
        return this.terminalTypeMapper.selectObjByMap(params);
    }

    @Override
    public List<TerminalType> selectObjAll() {
        return this.terminalTypeMapper.selectObjAll();
    }
}
