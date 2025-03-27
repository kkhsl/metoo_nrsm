package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.dto.UnitDTO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit;

import java.util.List;
import java.util.Map;

public interface IUnitService {

    Unit selectObjById(Long id);

    List<Unit> selectObjByMap(Map params);

    List<Unit> selectObjByMapToMonitor(Map params);

    int update(Unit instance);

    Result selectObjConditionQuery(UnitDTO dto);
    Result selectAllQuery();

    Result add();

    Result save(Unit instance);

    Result delete(String ids);
}
