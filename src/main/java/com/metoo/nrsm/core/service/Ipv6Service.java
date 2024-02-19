package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.nspm.Ipv6;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:30
 */
public interface Ipv6Service {

    boolean save(Ipv6 instance);

    boolean removeDuplicates();

    boolean truncateTable();
}
