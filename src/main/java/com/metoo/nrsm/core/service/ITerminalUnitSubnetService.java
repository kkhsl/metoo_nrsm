package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.TerminalUnit;
import com.metoo.nrsm.entity.TerminalUnitSubnet;

import java.util.List;
import java.util.Map;

public interface ITerminalUnitSubnetService {

    TerminalUnitSubnet selectObjById(Long id);

    List<TerminalUnitSubnet> selectObjByMap(Map params);
}
