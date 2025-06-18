package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.GradeWeight;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-30 15:19
 */
@Mapper
public interface GradeWeightMapper {

    GradeWeight selectObjOne();

    boolean update(GradeWeight instance);
}
