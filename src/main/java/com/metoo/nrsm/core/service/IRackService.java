package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.RackDTO;
import com.metoo.nrsm.entity.Rack;

import java.util.List;
import java.util.Map;

public interface IRackService {

    Rack getObjById(Long id);

    Rack selectObjByName(String name);

    Page<Rack> findBySelect(RackDTO instance);

    List<Rack> query(Rack rack);

    List<Rack> selectObjByMap(Map params);

    int save(Rack instance);

    int batchInsert(List<Rack> rackList);

    int update(Rack instance);

    int delete(Long id);

    Object batchDel(String ids);

    // 机柜信息（包含机柜空闲位置，以及设备信息，正反面）
    Object rack(Long id);

    boolean verifyRack(Rack rack, Integer start, Integer size, boolean rear, Long id);
}
