package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.FluxConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 11:30
 */
@Mapper
public interface FluxConfigMapper {

    FluxConfig selectObjById(Long id);

    List<FluxConfig> selectObjByMap(Map params);

    int save(FluxConfig instance);

    int update(FluxConfig instance);

    int delete(Long id);
}
