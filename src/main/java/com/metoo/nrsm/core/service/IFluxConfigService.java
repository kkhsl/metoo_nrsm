package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.FluxConfig;

import java.util.List;
import java.util.Map;


public interface IFluxConfigService {

    FluxConfig selectObjById(Long id);

    List<FluxConfig> selectObjByMap(Map params);

    boolean save(FluxConfig instance);

    boolean update(FluxConfig instance);

    boolean delete(Long id);
}
