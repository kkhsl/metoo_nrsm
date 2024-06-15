package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Ipv4Detail;

import java.util.List;
import java.util.Map;

public interface Ipv4DetailService {

    Ipv4Detail selectObjByIp(String ip);

    List<Ipv4Detail> selectObjByMap(Map map);

    Ipv4Detail selectObjByMac(String mac);

    int save(Ipv4Detail instance);

    int update(Ipv4Detail instance);
}
