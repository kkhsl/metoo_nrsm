package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.DnsRunStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 10:12
 */
@Mapper
public interface DnsRunStatusMapper {

    DnsRunStatus selectOneObj();

    boolean update(DnsRunStatus install);
}
