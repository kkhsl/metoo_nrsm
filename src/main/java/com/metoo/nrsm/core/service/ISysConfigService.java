package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.SysConfig;

public interface ISysConfigService {

    SysConfig findObjById(Long id);

    SysConfig  select();

    int modify(SysConfig instance);

    boolean update(SysConfig instance);
}
