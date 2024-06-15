package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Nswitch;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-17 17:04
 */
public interface INswitchService {

    Nswitch selectObjByName(String name);

    List<Nswitch> selectObjAll();

    boolean save(Nswitch instance);

}
