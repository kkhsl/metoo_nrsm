package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.TerminalUnit;
import com.metoo.nrsm.entity.TerminalUnitSubnet;
import com.metoo.nrsm.entity.TerminalUnitSubnetV6;
import com.metoo.nrsm.entity.UnitSubnet;

import java.util.List;
import java.util.Map;

public interface ITerminalUnitService {

    TerminalUnit selectObjById(Long id);

    List<TerminalUnit> selectObjAll();
    void add(TerminalUnit terminalUnit);
    void addV4(TerminalUnitSubnet TerminalUnitSubnet);

    void addV6(TerminalUnitSubnetV6 terminalUnitSubnetV6);

    int delete(Long id);
    int deleteV4(Long id);
    int deleteV6(Long id);

    List<TerminalUnit> selectObjByMap(Map params);

    List<TerminalUnit> selectObjAndTerminalByMap(Map params);

    List<TerminalUnit> selectObjAndTerminalHistoryByMap(Map params);

}
