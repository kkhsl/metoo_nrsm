package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.nspm.Ipv6;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:31
 */
@Mapper
public interface Ipv6Mapper {

    // 去重
    boolean removeDuplicates();

    boolean save(Ipv6 instance);

    boolean truncateTable();
}
