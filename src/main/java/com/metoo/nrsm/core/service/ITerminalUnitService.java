package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.TerminalUnit;

import java.util.List;
import java.util.Map;

public interface ITerminalUnitService {

    TerminalUnit selectObjById(Long id);

    List<TerminalUnit> selectObjAll();

    List<TerminalUnit> selectObjByMap(Map params);

    List<TerminalUnit> selectObjAndTerminalByMap(Map params);

    List<TerminalUnit> selectObjAndTerminalHistoryByMap(Map params);

}
