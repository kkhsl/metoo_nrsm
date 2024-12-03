package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Probe;

import java.util.List;
import java.util.Map;

public interface IProbeService {

    List<Probe> selectObjByMap(Map params);

    List<Probe> mergeProbesByIp();

    List<Probe> selectDeduplicationByIp(Map params);

    boolean insert(Probe instance);

    boolean update(Probe instance);

    int delete(Integer id);

    int deleteTable();

    int deleteTableBack();

    int copyToBck();

    boolean deleteProbeByIp(String ipv4, String ipv6);

    // 扫描
    void scanByTerminal();
}
