package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.MacVendorDeviceType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MacVendorDeviceTypeMapper {

    MacVendorDeviceType selectObjByMacVendor(String macVendor);
}
