package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Ipv4Detail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface Ipv4DetailMapper {

    List<Ipv4Detail> selectObjByMap(Map map);

    Ipv4Detail selectObjByMac(String mac);

    Ipv4Detail selectObjByIp(String ip);

    int save(Ipv4Detail instance);

    int update(Ipv4Detail instance);

    void truncateTable();
}
