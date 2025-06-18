package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.TerminalUnit;
import com.metoo.nrsm.entity.UnitSubnet;

import java.util.List;

public interface IUnitSubnetService {

    List<UnitSubnet> selectObjAll();
}
