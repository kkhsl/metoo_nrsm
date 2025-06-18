package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit;

import java.util.List;

public interface IUnitService {

    Unit selectObjById(Long id);

    int update(Unit instance);

    Result selectObjConditionQuery(UnitNewDTO dto);

    Result selectAllQuery();

    List<Unit> selectUnitAll();

    Result save(Unit instance);

    Result delete(String ids);
}
