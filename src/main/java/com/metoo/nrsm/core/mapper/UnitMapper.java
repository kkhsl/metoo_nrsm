package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.entity.Unit;

import java.util.List;

public interface UnitMapper {

    Unit selectObjById(Long id);

    int update(Unit instance);

    List<Unit> selectObjConditionQuery(UnitNewDTO dto);

    List<Unit> selectAllQuery();

    int save(Unit instance);

    int delete(Long id);
    int countByUnitName(String unitName);

}
