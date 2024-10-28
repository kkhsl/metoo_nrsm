package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.FluxDailyRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-21 10:54
 */
public interface FluxDailyRateMapper {

    List<FluxDailyRate> selectObjByMap(Map params);

    int save(FluxDailyRate instance);
}
