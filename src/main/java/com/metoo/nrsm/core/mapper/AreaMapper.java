package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Area;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AreaMapper{
    // 查询指定层级的所有地区
    List<Area> findByLevel(@Param("level") Integer level);

    Area findByCode(@Param("code") String code);

    // 根据父编码查询子地区
    List<Area> findByParentCode(@Param("parentCode") String parentCode);

    // 批量查询多个父编码对应的子地区
    List<Area> findByParentCodes(@Param("parentCodes") List<String> parentCodes);
}