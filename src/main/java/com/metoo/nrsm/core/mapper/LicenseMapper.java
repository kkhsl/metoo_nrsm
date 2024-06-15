package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.License;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LicenseMapper {

    List<License> query();

    int update(License instance);

    int save(License instance);
}
