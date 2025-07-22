package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.AreaMapper;
import com.metoo.nrsm.entity.Area;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AreaServiceImpl {
    private static final int CITY_LEVEL = 2; // 市级
    private static final int COUNTY_LEVEL = 3; // 区县级

    @Autowired
    private AreaMapper areaMapper;

    // 获取所有市级行政区
    public List<Area> getCities() {
        return areaMapper.findByLevel(CITY_LEVEL);
    }

    // 获取完整的省市区县树形结构
    public List<Area> getFullAreaTree() {
        // 获取所有市级行政区
        List<Area> cities = getCities();
        if (cities == null || cities.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取所有市级编码
        List<String> cityCodes = cities.stream()
                .map(Area::getCode)
                .collect(Collectors.toList());

        // 批量查询所有区县级数据
        List<Area> counties = areaMapper.findByParentCodes(cityCodes);

        // 按父级编码分组
        Map<String, List<Area>> countyMap = counties.stream()
                .collect(Collectors.groupingBy(Area::getParentCode));

        // 构建树形结构
        cities.forEach(city -> {
            List<Area> children = countyMap.getOrDefault(city.getCode(),
                    Collections.emptyList());
            city.setChildren(children);
        });

        return cities;
    }


}
