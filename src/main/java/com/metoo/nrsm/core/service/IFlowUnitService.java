package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowUnit;

import java.util.List;
import java.util.Map;

public interface IFlowUnitService {

    FlowUnit selectObjById(Long id);

    List<FlowUnit> selectByUnitId(Long unitId);

    List<FlowUnit> selectObjByMap(Map params);

    List<FlowUnit> selectObjByMapToMonitor(Map params);

    int update(FlowUnit instance);

    Result selectObjConditionQuery(UnitNewDTO dto);

    Result selectAllQuery();

    Result add();

    Result save(FlowUnit instance);

    Result delete(String ids);
}
