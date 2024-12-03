package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.ProbeResult;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-25 21:30
 */
public interface IProbeResultService {

    ProbeResult selectObjByOne();

    List<ProbeResult> selectObjByMap(Map params);

    int update(ProbeResult result);

}
