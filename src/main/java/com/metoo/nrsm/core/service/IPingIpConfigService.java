package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.PingIpConfig;

import java.util.List;

public interface IPingIpConfigService {

    PingIpConfig selectOneObj();

    boolean update(PingIpConfig install);

    boolean checkaliveip();

    boolean status();

    boolean start();

    boolean stop();

    boolean restart();
}
