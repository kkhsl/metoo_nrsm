package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.GradeWeight;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-30 15:16
 */
public interface IGradWeightService {

    GradeWeight selectObjOne();

    boolean update(GradeWeight instance);
}
