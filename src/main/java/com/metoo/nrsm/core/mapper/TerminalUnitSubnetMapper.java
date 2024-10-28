package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.TerminalUnit;
import com.metoo.nrsm.entity.TerminalUnitSubnet;

import java.util.List;
import java.util.Map;

public interface TerminalUnitSubnetMapper {

    TerminalUnitSubnet selectObjById(Long id);

    List<TerminalUnitSubnet> selectObjByMap(Map params);
}
