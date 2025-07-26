package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.dto.GatewayDTO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Gateway;

import java.util.List;
import java.util.Map;

public interface IGatewayService {

    Gateway selectObjById(Long id);

    List<Gateway> selectObjByMap(Map params);

    int update(Gateway instance);

    Result selectObjConditionQuery(GatewayDTO dto);

    Result save(Gateway instance) throws Exception;

    Result delete(String ids);

    Result batchSave(List<Gateway> devices);

    Result modify(Long id);
}
