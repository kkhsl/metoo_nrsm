package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.FluxDailyRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-21 10:44
 */
public interface IFluxDailyRateService {

    List<FluxDailyRate> selectObjByMap(Map params);

    boolean save(FluxDailyRate instance);
}
