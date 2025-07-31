package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.entity.Interface;

import java.util.List;
import java.util.Map;

public interface IInterfaceService {

    Interface selectObjById(Long id);

    List<Interface> selectObjByParentId(Long parentId);

    Interface selectObjByName(String name);

    List<Interface> selectParentInterfaces(List<Long> parentIds);

    Page<Interface> selectObjConditionQuery(InterfaceDTO dto);

    List<Interface> selectObjByMap(Map params);

    List<Interface> selectAll();

    int save(Interface instance);

    List<Interface> select();

    int update(Interface instance);

    void truncate();

    boolean modify_ip(Interface instance);

    // TODO Vlan改用Interface
    boolean modify_vlans(String name, Interface instance);

    int delete(Long id);

}
