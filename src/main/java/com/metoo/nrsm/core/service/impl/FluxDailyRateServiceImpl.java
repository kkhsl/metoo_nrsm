package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.FluxDailyRateMapper;
import com.metoo.nrsm.core.service.IFluxDailyRateService;
import com.metoo.nrsm.core.service.IGradWeightService;
import com.metoo.nrsm.entity.FluxDailyRate;
import com.metoo.nrsm.entity.GradeWeight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-21 10:53
 */
@Service
@Transactional
public class FluxDailyRateServiceImpl implements IFluxDailyRateService {

    @Resource
    private FluxDailyRateMapper fluxDailyRateMapper;

    @Override
    public List<FluxDailyRate> selectObjByMap(Map params) {
        return this.fluxDailyRateMapper.selectObjByMap(params);
    }

    @Override
    public boolean save(FluxDailyRate instance) {
        try {
            this.fluxDailyRateMapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
