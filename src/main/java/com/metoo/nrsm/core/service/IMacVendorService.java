package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.MacVendor;

import java.util.List;
import java.util.Map;

public interface IMacVendorService {

    List<MacVendor> selectObjByMap(Map params);
}
