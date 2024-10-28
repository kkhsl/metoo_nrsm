package com.metoo.nrsm.core.service;


import com.metoo.nrsm.entity.Vendor;

import java.util.List;
import java.util.Map;

public interface IVendorService {

    Vendor selectObjById(Long id);

    Vendor selectObjByName(String name);

    List<Vendor> selectObjByMap(Map params);

    List<Vendor> selectConditionQuery(Map params);
}
