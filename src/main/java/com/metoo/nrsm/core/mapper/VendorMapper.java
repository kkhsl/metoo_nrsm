package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Vendor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface VendorMapper {

    Vendor selectObjById(Long id);

    Vendor selectObjByName(String name);

    List<Vendor> selectObjByMap(Map params);

    List<Vendor> selectConditionQuery(Map params);

    List<Vendor> selectByDeviceType(Long id);

}
