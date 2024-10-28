package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.IpStatisticsResult;

import java.util.List;

public interface IpStatisticsResultMapper {

    List<IpStatisticsResult> selectObjByTop();
}
