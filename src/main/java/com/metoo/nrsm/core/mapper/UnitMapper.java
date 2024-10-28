package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.GatewayDTO;
import com.metoo.nrsm.core.dto.UnitDTO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit;

import java.util.List;
import java.util.Map;

public interface UnitMapper {

    Unit selectObjById(Long id);

    List<Unit> selectObjByMap(Map params);

    List<Unit> selectObjByMapToMonitor(Map params);

    int update(Unit instance);

    List<Unit> selectObjConditionQuery(UnitDTO dto);

    int save(Unit instance);

    int delete(Long id);

}
