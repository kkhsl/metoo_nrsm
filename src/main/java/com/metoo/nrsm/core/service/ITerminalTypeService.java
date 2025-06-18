package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.TerminalType;

import java.util.List;
import java.util.Map;

public interface ITerminalTypeService {

    TerminalType selectObjById(Long id);

    TerminalType selectObjByType(Integer type);

    List<TerminalType> selectObjByMap(Map params);

    List<TerminalType> selectObjAll();
}

