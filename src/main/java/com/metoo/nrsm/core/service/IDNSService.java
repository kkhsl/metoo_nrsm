package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Dns;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-29 16:07
 */
public interface IDNSService {

    List<Dns> selectObjByMap(Map params);

    List<Dns> selectObjByPrimaryDomain(String primaryDomain);

    boolean save(Dns instance);

    boolean update(Dns instance);

    boolean delete(Long id);

    String get();

    String modifydns(String[] params);
}
