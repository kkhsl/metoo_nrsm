package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.nspm.Arp;
import com.metoo.nrsm.entity.nspm.Ipv4;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:31
 */
@Mapper
public interface Ipv4Mapper {

    List<Ipv4> joinSelectObjAndIpv6();

    boolean save(Ipv4 instance);

    // 去重
    boolean removeDuplicates();

    boolean truncateTable();


}
