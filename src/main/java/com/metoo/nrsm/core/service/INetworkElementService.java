package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.NetworkElementDto;
import com.metoo.nrsm.entity.NetworkElement;

import java.util.List;
import java.util.Map;

public interface INetworkElementService {

    NetworkElement selectObjById(Long id);

    NetworkElement selectObjByUuid(String uuid);

    Page<NetworkElement> selectConditionQuery(NetworkElementDto instance);
    List<NetworkElement> selectConditionByIpQuery(List<String> instance);

    List<NetworkElement> selectObjByMap(Map params);

    List<NetworkElement> selectObjAll();

    List<NetworkElement> selectObjAllByGather();

    int save(NetworkElement instance);

    int update(NetworkElement instance);

    int updateObjDisplay();

    int delete(Long id);

    int batchInsert(List<NetworkElement> instance);

    NetworkElement selectAccessoryByUuid(String uuid);

}
