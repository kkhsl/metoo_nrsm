package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.PingIpConfig;

import java.util.List;

public interface IPingIpConfigService {

    PingIpConfig selectOneObj();

    boolean update(PingIpConfig install);

    Result save(PingIpConfig install);

    boolean checkaliveip();

    /**
     * 检查程序状态
     *
     * @return
     */
    boolean status();

    boolean start();

    boolean stop();

    boolean restart();
}
