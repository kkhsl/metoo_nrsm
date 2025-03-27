package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.UnitDTO;
import com.metoo.nrsm.entity.Unit;

import java.util.List;
import java.util.Map;

public interface UnitMapper {

    Unit selectObjById(Long id);

    List<Unit> selectObjByMap(Map params);

    List<Unit> selectObjByMapToMonitor(Map params);

    int update(Unit instance);

    List<Unit> selectObjConditionQuery(UnitDTO dto);
    List<Unit> selectAllQuery();

    int save(Unit instance);

    int delete(Long id);

}
