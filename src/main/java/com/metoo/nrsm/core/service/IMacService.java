package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.nspm.Mac;

import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:19
 */
public interface IMacService {

    boolean save(Mac instance);

    boolean truncateTable();

    void gatherMac(Date date);
}
