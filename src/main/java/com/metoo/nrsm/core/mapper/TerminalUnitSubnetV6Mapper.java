package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.TerminalUnitSubnet;
import com.metoo.nrsm.entity.TerminalUnitSubnetV6;

import java.util.List;
import java.util.Map;

public interface TerminalUnitSubnetV6Mapper {

    TerminalUnitSubnetV6 selectObjById(Long id);

    List<TerminalUnitSubnetV6> selectObjByMap(Map params);
}
