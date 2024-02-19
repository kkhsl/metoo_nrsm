package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.nspm.Mac;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:21
 */
@Mapper
public interface MacMapper {

    int save(Mac instance);

    int truncateTable();
}
