package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Unit2;

import java.util.List;

public interface Unit2Mapper {

    Unit2 selectObjById(Long id);

    int update(Unit2 instance);

    List<Unit2> selectObjConditionQuery(Unit2 dto);
    List<Unit2> selectAllQuery();

    int save(Unit2 instance);

    int delete(Long id);

}
