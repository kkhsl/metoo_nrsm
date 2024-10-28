package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Arp;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:21
 */
@Mapper
public interface ArpMapper {

    List<Arp> selectObjDistinctV4ip();

    List<Arp> selectObjByMap(Map params);

    List<Arp> joinSelectObjAndIpv6();

    List<Arp> mergeIpv4AndIpv6(Map params);

    int save(Arp instance);

    int writeArp();

    int truncateTable();

    int deleteTable();

    int truncateTableGather();

    int saveGather(Arp instance);

    int batchSaveGather(List<Arp> instance);

    int batchSaveGatherBySelect(Map params);

    int batchSaveIpV4AndIpv6ToArpGather(Map params);

    int copyGatherDataToArp();

    int gathreArp(Date date);
}
