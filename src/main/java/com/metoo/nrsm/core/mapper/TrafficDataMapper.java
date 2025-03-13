package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.TrafficData;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface TrafficDataMapper {
    void insertTrafficData(TrafficData trafficData);
}