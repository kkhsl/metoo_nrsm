package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.PingStartParam;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 9:57
 */
public interface IPingStartServiceParam {

    PingStartParam selectOneObj();

    boolean update(PingStartParam install);

}
