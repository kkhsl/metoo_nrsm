package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.IpStatisticsResult;

import java.util.List;

public interface IpStatisticsResultService {

    List<IpStatisticsResult> selectObjByTop();

}
