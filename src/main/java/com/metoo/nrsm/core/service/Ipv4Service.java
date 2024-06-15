package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Ipv4;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:30
 */
public interface Ipv4Service {

    List<Ipv4> selectDuplicatesObjByMap(Map params);

    List<Ipv4> joinSelectObjAndIpv6();

    boolean save(Ipv4 instance);

    boolean removeDuplicates();

    boolean truncateTable();

    boolean deleteTable();

    boolean saveGather(Ipv4 instance);

    boolean batchSaveGather(List<Ipv4> instance);

    boolean truncateTableGather();

    boolean clearAndcopyGatherDataToIpv4();


}
