package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.MacDTO;
import com.metoo.nrsm.entity.Mac;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:21
 */
@Mapper
public interface MacMapper {

    List<Mac> selectObjByMap(Map params);

    List<Mac> selectTagByDE();

    List<Mac> selectTagDEWithoutNswitch();

    List<Mac> selectTagDEWithNswitch();

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

    List<Mac> copyArpIpToMacByDT(Map params);

    List<Mac> selectDTToDEByMap(Map params);

    List<Mac> selectRTToDEByMap(Map params);

    List<Mac> selectRTToDTByDE();

    List<Mac> selectRTToVDT();

    // 查询DT，合并多个ip地址
    List<Mac> selectDTByMap(Map params);


    List<Mac> selectDTAndDynamicByMap(Map params);

    List<Mac> selectDTAndDynamicByConditionQuery(MacDTO instance);

    int save(Mac instance);

    int update(Mac instance);

    /**
     * <!--
     * collection 指定要遍历的集合
     * list类型的参数会特殊处理封装在map中，map的key就叫list
     * item 将当前遍历出的元素赋值给指定 的变量
     * #{变量名} 就能取出变量的值也就是当前遍历
     * separator 每个元素之间的分隔符
     * open 遍历出所有结果拼接一个开始的字符
     * close: 遍历出所有结果拼接一个结束的字符
     * index : 索引  遍历list的时候index是索引  ,item就是当前值
     * 遍历map的时候index表示的就是map的key    item就是map的值
     * -->
     *
     * @param ids collection
     * @return
     */
    int updateMacTagToRTByIds(@Param("ids") Set<Long> ids);

    int updateMacTagToDTByIds(@Param("ids") Set<Long> ids);

    int normalizePortForDE();

    int safelyDeleteDuplicateDEIpPairs();

    int batchSave(List<Mac> instance);

    int batchUpdate(List<Mac> instance);

    int saveGather(Mac instance);

    int batchSaveGather(List<Mac> instance);

    int truncateTable();

    int truncateTableGather();

    int deleteTable();

    int copyGatherDataToMac(Date addTime);

    // 加锁
    void lock();

    // 释放锁
    void releaseLock();

    // 查看锁
    int queryLock();

    int copyDataToMacHistory();

}
