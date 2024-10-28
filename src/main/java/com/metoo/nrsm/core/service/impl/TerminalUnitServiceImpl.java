package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.service.ITerminalUnitService;
import com.metoo.nrsm.entity.TerminalUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TerminalUnitServiceImpl implements ITerminalUnitService {

    @Resource
    private TerminalUnitMapper terminalUnitMapper;

    @Override
    public TerminalUnit selectObjById(Long id) {
        return this.terminalUnitMapper.selectObjById(id);
    }

    @Override
    public List<TerminalUnit> selectObjAll() {
        return this.terminalUnitMapper.selectObjAll();
    }

    @Override
    public List<TerminalUnit> selectObjByMap(Map params) {
        return this.terminalUnitMapper.selectObjByMap(params);
    }

    @Override
    public List<TerminalUnit> selectObjAndTerminalByMap(Map params) {
        return this.terminalUnitMapper.selectObjAndTerminalByMap(params);
    }

    @Override
    public List<TerminalUnit> selectObjAndTerminalHistoryByMap(Map params) {
        return this.terminalUnitMapper.selectObjAndTerminalHistoryByMap(params);
    }
}
