package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Traffic;

import java.util.List;
import java.util.Map;

public interface ITrafficService {

    Traffic selectObjById(Long id);

    List<Traffic> selectObjByMap(Map params);

    int save(Traffic instance);

}
