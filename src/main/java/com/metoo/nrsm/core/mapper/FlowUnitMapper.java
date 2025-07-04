package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.entity.FlowUnit;

import java.util.List;
import java.util.Map;

public interface FlowUnitMapper {

    FlowUnit selectObjById(Long id);

    List<FlowUnit> selectObjByMap(Map params);

    List<FlowUnit> selectByUnitId(Long unitId);

    List<FlowUnit> selectObjByMapToMonitor(Map params);

    int update(FlowUnit instance);

    List<FlowUnit> selectObjConditionQuery(UnitNewDTO dto);

    List<FlowUnit> selectAllQuery();

    int save(FlowUnit instance);

    int delete(Long id);

}
