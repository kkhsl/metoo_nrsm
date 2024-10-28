package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Ping;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 17:38
 */
public interface IPingService {

    Ping selectOneObj();

    void killDns();

    void startDns(String param);

    void exec();
}
