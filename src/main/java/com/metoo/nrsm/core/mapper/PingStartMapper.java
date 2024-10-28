package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.PingStartParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 9:59
 */
@Mapper
public interface PingStartMapper {

    PingStartParam selectOneObj();

    boolean update(PingStartParam install);
}
