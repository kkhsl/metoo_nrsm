package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.ProbeResult;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-29 20:47
 */
public interface ProbeResultMapper {

    ProbeResult selectObjByOne();

    List<ProbeResult> selectObjByMap(Map params);

    int update(ProbeResult result);

}
