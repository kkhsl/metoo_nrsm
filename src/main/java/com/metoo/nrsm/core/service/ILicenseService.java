package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.License;

import java.util.List;

public interface ILicenseService {

    /**
     * 根据UUID检测是否为被允许设备
     */
    License detection();

    List<License> query();

    int save(License instance);

    int update(License instance);

}
