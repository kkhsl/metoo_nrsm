package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.service.ITerminalUnitService;
import com.metoo.nrsm.entity.TerminalUnit;
import com.metoo.nrsm.entity.TerminalUnitSubnet;
import com.metoo.nrsm.entity.TerminalUnitSubnetV6;
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
        List<TerminalUnit> terminalUnits = terminalUnitMapper.selectObjAll();
        for (TerminalUnit terminalUnit : terminalUnits) {
            if (terminalUnitMapper.selectV4ObjByTerminalUnitId(terminalUnit.getId())!=null){
                terminalUnit.setTerminaV4lList(terminalUnitMapper.selectV4ObjByTerminalUnitId(terminalUnit.getId()));
            }
            if (terminalUnitMapper.selectV6ObjByTerminalUnitId(terminalUnit.getId())!=null){
                terminalUnit.setTerminaV6lList(terminalUnitMapper.selectV6ObjByTerminalUnitId(terminalUnit.getId()));
            }
        }
        return terminalUnits;
    }

    @Override
    public void add(TerminalUnit terminalUnit) {
        if (terminalUnitMapper.selectObjById(terminalUnit.getId()) != null) {
            terminalUnitMapper.updateTerminalUnit(terminalUnit);
        } else {
            terminalUnitMapper.insertTerminalUnit(terminalUnit);
        }
    }

    @Override
    public void addV4(TerminalUnitSubnet terminalUnitSubnet) {
        if (terminalUnitMapper.selectV4ObjById(terminalUnitSubnet.getId()) != null) {
            terminalUnitMapper.updateTerminalUnitV4(terminalUnitSubnet);
        } else {
            terminalUnitMapper.insertTerminalUnitV4(terminalUnitSubnet);
        }
    }


    @Override
    public void addV6(TerminalUnitSubnetV6 terminalUnitSubnetV6) {
        if (terminalUnitMapper.selectV6ObjById(terminalUnitSubnetV6.getId()) != null) {
            terminalUnitMapper.updateTerminalUnitV6(terminalUnitSubnetV6);
        } else {
            terminalUnitMapper.insertTerminalUnitV6(terminalUnitSubnetV6);
        }
    }

    @Override
    public int delete(Long id) {
        terminalUnitMapper.deleteTerminalUnitV4ByTerminalUnitId(id);
        terminalUnitMapper.deleteTerminalUnitV6ByTerminalUnitId(id);
        return terminalUnitMapper.deleteTerminalUnit(id);
    }

    @Override
    public int deleteV4(Long id) {
        return terminalUnitMapper.deleteTerminalUnitV4(id);
    }
    @Override
    public int deleteV6(Long id) {
        return terminalUnitMapper.deleteTerminalUnitV6(id);
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
