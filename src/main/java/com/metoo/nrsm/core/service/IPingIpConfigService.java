package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.PingIpConfig;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 16:33
 */
public interface IPingIpConfigService {

    PingIpConfig selectOneObj();

    boolean update(PingIpConfig install);

    boolean checkaliveip();

    boolean status();

    boolean start();

    boolean stop();

    boolean restart();
}
