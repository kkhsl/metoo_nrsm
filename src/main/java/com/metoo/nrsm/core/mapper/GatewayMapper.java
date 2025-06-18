package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.GatewayDTO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Gateway;

import java.util.List;
import java.util.Map;

public interface GatewayMapper {

    Gateway selectObjById(Long id);

    List<Gateway> selectObjByMap(Map params);

    int update(Gateway instance);

    List<Gateway> selectObjConditionQuery(GatewayDTO dto);

    int save(Gateway instance);

    int delete(Long id);
}
