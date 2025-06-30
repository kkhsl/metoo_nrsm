package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Probe;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:31
 */
@Mapper
public interface ProbeMapper {

    List<Probe> selectObjByMap(Map params);

    List<Probe> selectProbeBackByMap(Map params);

    List<Probe> updateBackupToIp();

    int syncProbeIpWithTerminal();

    List<Probe> findDiffBetweenProbeAndBackup();

    int syncProbeDiffToBackup();

    List<String> selectObjDistinctByIp();

    List<Probe> mergeProbesByIp();

    int insert(Probe instance);

    int update(Probe instance);

    int delete(Integer id);

    int deleteTable();

    int deleteTableBack();

    int copyToBck();

}
