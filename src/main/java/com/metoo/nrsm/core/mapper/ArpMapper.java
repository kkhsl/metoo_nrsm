package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.nspm.Arp;
import com.metoo.nrsm.entity.nspm.Ipv4;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:21
 */
@Mapper
public interface ArpMapper {

    List<Arp> selectObjByMap(Map params);

    List<Arp> joinSelectObjAndIpv6();

    int save(Arp instance);

    int writeArp();

    int truncateTable();
}
