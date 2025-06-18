package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.VlanDTO;
import com.metoo.nrsm.entity.Vlan;

import java.util.List;
import java.util.Map;

public interface IVlanService {

    Vlan selectObjById(Long id);

    Page<Vlan> selectObjConditionQuery(VlanDTO dto);

    List<Vlan> selectObjByMap(Map params);

    int save(Vlan instance);

    int update(Vlan instance);

    int delete(Long id);

}
