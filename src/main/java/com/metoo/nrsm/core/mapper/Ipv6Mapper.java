package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Ipv6;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:31
 */
@Mapper
public interface Ipv6Mapper {

    // 去重
    int removeDuplicates();

    int save(Ipv6 instance);

    int truncateTable();

    int deleteTable();

    int saveGather(Ipv6 instance);

    int batchSaveGather(List<Ipv6> instance);

    int truncateTableGather();

    int copyGatherToIpv6();
}
