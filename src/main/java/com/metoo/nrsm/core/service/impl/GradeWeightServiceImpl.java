package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.GradeWeightMapper;
import com.metoo.nrsm.core.service.IGradWeightService;
import com.metoo.nrsm.entity.GradeWeight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-30 15:19
 */

@Service
@Transactional
public class GradeWeightServiceImpl implements IGradWeightService {

    @Autowired
    private GradeWeightMapper gradeWeightMapper;

    @Override
    public GradeWeight selectObjOne() {
        return this.gradeWeightMapper.selectObjOne();
    }


    @Override
    public boolean update(GradeWeight instance) {
        try {
            this.gradeWeightMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
