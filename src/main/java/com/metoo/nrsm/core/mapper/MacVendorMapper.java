package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.MacVendor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface MacVendorMapper {

    List<MacVendor> selectObjByMap(Map params);
}
