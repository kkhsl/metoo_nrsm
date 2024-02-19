package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.nspm.Arp;
import com.metoo.nrsm.entity.nspm.Ipv4;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:30
 */
public interface Ipv4Service {

    List<Ipv4> joinSelectObjAndIpv6();

    boolean save(Ipv4 instance);

    boolean removeDuplicates();

    boolean truncateTable();
}
