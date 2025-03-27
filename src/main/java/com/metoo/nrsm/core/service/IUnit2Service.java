package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.dto.UnitDTO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit2;

public interface IUnit2Service {

    Unit2 selectObjById(Long id);

    int update(Unit2 instance);

    Result selectObjConditionQuery(UnitDTO dto);
    Result selectAllQuery();
    Result save(Unit2 instance);

    Result delete(String ids);
}
