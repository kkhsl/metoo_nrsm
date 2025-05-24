package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.RadvdDTO;
import com.metoo.nrsm.entity.Radvd;

import java.util.List;
import java.util.Map;

public interface IRadvdService {

    Radvd selectObjById(Long id);

    Radvd selectObjByName(String name);

    List<Radvd> selectObjByMap(Map params);

    Page<Radvd> selectObjConditionQuery(RadvdDTO dto);

    boolean save(Radvd instance);

    boolean update(Radvd instance);

    boolean delete(Long id);
}
