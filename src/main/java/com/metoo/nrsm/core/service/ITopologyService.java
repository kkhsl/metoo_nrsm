package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.TopologyDTO;
import com.metoo.nrsm.entity.Port;
import com.metoo.nrsm.entity.Topology;

import java.util.List;
import java.util.Map;

public interface ITopologyService {

    Topology selectObjById(Long id);

    Topology selectObjBySuffix(String name);

    Page<Topology> selectConditionQuery(TopologyDTO instance);

    List<Topology> selectObjByMap(Map params);

    List<Topology> selectObjHistoryByMap(Map params);

    List<Topology> selectTopologyByMap(Map params);


    int saveHistory(Topology instance);

    int save(Topology instance);

    int update(Topology instance);

    int delete(Long id);

    Long copy(Topology instance);

    List<Port> getDevicePortsByUuid(String uuid);

    List<Map<String, Object>> getTerminalPortsByUuid(String uuid);

}
