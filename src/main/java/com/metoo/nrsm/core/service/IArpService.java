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

    boolean writeArp();

    boolean truncateTable();

    void gatherArp(Date date);
}
