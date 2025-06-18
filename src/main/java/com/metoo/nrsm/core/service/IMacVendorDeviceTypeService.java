package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.MacVendorDeviceType;

public interface IMacVendorDeviceTypeService {

    MacVendorDeviceType selectObjByMacVendor(String macVendor);

}
