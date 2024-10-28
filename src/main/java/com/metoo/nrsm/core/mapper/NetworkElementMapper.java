package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.NetworkElementDto;
import com.metoo.nrsm.entity.NetworkElement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface NetworkElementMapper {

    NetworkElement selectObjById(Long id);

    NetworkElement selectObjByUuid(String uuid);

    List<NetworkElement> selectConditionQuery(NetworkElementDto instance);

    List<NetworkElement> selectObjByMap(Map params);

    List<NetworkElement> selectObjAll(Map params);

    List<NetworkElement> selectObjAllByGather();

    int save(NetworkElement instance);

    int batchInsert(List<NetworkElement> instance);

    int update(NetworkElement instance);

    int delete(Long id);

    NetworkElement selectAccessoryByUuid(String uuid);

}
