package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.entity.Interface;
import com.metoo.nrsm.entity.Vlans;

import java.util.List;
import java.util.Map;

public interface IInterfaceService {

    Interface selectObjById(Long id);

    Page<Interface> selectObjConditionQuery(InterfaceDTO dto);

    List<Interface> selectObjByMap(Map params);

    int save(Interface instance);

    int update(Interface instance);

    boolean modify_ip(Interface instance);
    boolean modify_vlans(String name, Vlans instance);

    int delete(Long id);

}
