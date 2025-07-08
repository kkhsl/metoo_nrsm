package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Arp;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:19
 */
public interface IArpService {

    List<Arp> selectObjDistinctV4ip();

    List<Arp> selectObjByMap(Map params);

    List<Arp> getDeviceArpByUuid(String uuid);

    List<Arp> joinSelectObjAndIpv6();

    List<Arp> mergeIpv4AndIpv6(Map params);

    boolean writeArp();

    boolean truncateTable();

    boolean save(Arp instance);

    boolean gatherArp(Date date);

    boolean deleteTable();

    boolean truncateTableGather();

    boolean saveGather(Arp instance);

    boolean batchSaveGather(List<Arp> instance);

    boolean batchSaveGatherBySelect(Map params);

    boolean batchSaveIpV4AndIpv6ToArpGather(Map params);

    boolean copyGatherDataToArp();

}
