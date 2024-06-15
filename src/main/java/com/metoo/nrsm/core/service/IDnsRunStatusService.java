package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.DnsRunStatus;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 10:11
 */
public interface IDnsRunStatusService {

    DnsRunStatus selectOneObj();

    boolean update(DnsRunStatus install);

    boolean checkdns();

    void start();

    void stop();
}
