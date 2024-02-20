package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.nspm.Arp;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:19
 */
public interface IArpService {

    List<Arp> selectObjByMap(Map params);

    List<Arp> joinSelectObjAndIpv6();

    boolean writeArp();

    boolean truncateTable();

    boolean save(Arp instance);

    void gatherArp(Date date);

    boolean deleteTable();

    boolean truncateTableGather();

    boolean saveGather(Arp instance);

    boolean batchSaveGather(List<Arp> instance);

    boolean copyGatherDataToArp();
}
