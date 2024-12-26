package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.MacDTO;
import com.metoo.nrsm.entity.Mac;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:19
 */
public interface IMacService {

    List<Mac> selectObjByMap(Map params);

    List<Mac> selectTagByDE();

    List<Mac> selectTagToX(Map params);

    List<Mac> selectTagToU(Map params);

    List<Mac> selectTagToS(Map params);

    List<Mac> selectTagSToE(Map params);

    List<Mac> selectTagSToRT(Map params);

    List<Mac> selectDistinctObjByMap(Map params);

    List<Mac> copyArpMacAndIpToMac(Map params);

    List<Mac> selectXToEByMap(Map params);

    List<Mac> selectUToEByMap(Map params);

    List<Mac> selectXToUTByMap(Map params);

    List<Mac> selectUToRTByMap(Map params);

    List<Mac> selectRTToDTByMap(Map params);

    List<Mac> selectRTToDT2ByMap(Map params);

    List<Mac> selectDTByMap(Map params);

    List<Mac> copyArpIpToMacByDT(Map params);

    List<Mac> selectDTToDEByMap(Map params);

    List<Mac> selectRTToDEByMap(Map params);

    List<Mac> selectRTToDTByDE();

    List<Mac> selectRTToVDT();

    List<Mac> selectDTAndDynamicByMap(Map params);

    Page<Mac> selectDTAndDynamicByConditionQuery(MacDTO instance);

    boolean save(Mac instance);

    boolean update(Mac instance);

    boolean updateMacTagToRTByIds(Set<Long> ids);

    boolean updateMacTagToDTByIds(Set<Long> ids);

    boolean batchSave(List<Mac> instance);

    boolean batchUpdate(List<Mac> instance);

    boolean truncateTable();

    void gatherMac(Date date);

    boolean saveGather(Mac instance);

    boolean batchSaveGather(List<Mac> instance);

    boolean truncateTableGather();

    boolean deleteTable();

    boolean  copyGatherDataToMac(Date date);

    boolean  copyGather(Date date);

    // 加锁
    void lock();

    // 释放锁
    void releaseLock();

    // 查看锁
    int queryLock();
}
