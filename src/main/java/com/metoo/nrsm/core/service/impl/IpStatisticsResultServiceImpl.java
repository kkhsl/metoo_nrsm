package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.IpStatisticsResultMapper;
import com.metoo.nrsm.core.service.IpStatisticsResultService;
import com.metoo.nrsm.entity.IpStatisticsResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class IpStatisticsResultServiceImpl implements IpStatisticsResultService {

    @Resource
    private IpStatisticsResultMapper ipStatisticsResultMapper;

    @Override
    public List<IpStatisticsResult> selectObjByTop() {
        return this.ipStatisticsResultMapper.selectObjByTop();
    }
}
