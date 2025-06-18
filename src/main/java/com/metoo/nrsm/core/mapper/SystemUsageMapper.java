package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.vo.SystemUsageVO;
import com.metoo.nrsm.entity.SystemUsage;

import java.util.List;
import java.util.Map;

public interface SystemUsageMapper {

    List<SystemUsage> selectObjByMap(Map params);

    List<SystemUsageVO> selectObjVOByMap(Map params);

    int save(SystemUsage instance);
}
