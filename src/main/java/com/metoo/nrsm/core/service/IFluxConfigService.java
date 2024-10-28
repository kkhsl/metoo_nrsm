package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.FluxConfig;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 11:29
 */
public interface IFluxConfigService {

    FluxConfig selectObjById(Long id);

    List<FluxConfig> selectObjByMap(Map params);

    boolean save(FluxConfig instance);

    boolean update(FluxConfig instance);

    boolean delete(Long id);
}
