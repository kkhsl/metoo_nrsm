package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Ipv4;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:31
 */
@Mapper
public interface Ipv4Mapper {

    List<Ipv4> selectDuplicatesObjByMap(Map params);

    List<Ipv4> joinSelectObjAndIpv6();

    int save(Ipv4 instance);

    int removeDuplicates();

    int truncateTable();

    int deleteTable();

    int saveGather(Ipv4 instance);

    int batchSaveGather(List<Ipv4> instance);

    int truncateTableGather();

    int copyGatherToIpv4();



}
