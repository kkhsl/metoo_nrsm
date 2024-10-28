package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Ping;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 17:39
 */
@Mapper
public interface PingMapper {

    Ping selectOneObj();
}
