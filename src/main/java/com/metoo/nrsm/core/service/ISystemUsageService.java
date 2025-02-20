package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.SystemUsage;

import java.util.List;
import java.util.Map;

public interface ISystemUsageService {

    // 获取系统数据
    void saveSystemUsageToDatabase();

    List<SystemUsage> selectObjByMap(Map params);

    boolean save(SystemUsage instance);
}
