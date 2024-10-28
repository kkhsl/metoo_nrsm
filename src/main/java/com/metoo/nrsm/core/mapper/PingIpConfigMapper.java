package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.PingIpConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 16:35
 */
@Mapper
public interface PingIpConfigMapper {

    PingIpConfig selectOneObj();

    int update(PingIpConfig install);
}
