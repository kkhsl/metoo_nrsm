package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.OperationLogDTO;
import com.metoo.nrsm.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2023-11-07 16:15
 */
@Mapper
public interface OperationLogMapper {

    OperationLog selectObjById(Long id);

    List<OperationLog> selectObjConditionQuery(OperationLogDTO instance);

    List<OperationLog> selectObjByMap(Map params);

    int save(OperationLog instance);

    int update(OperationLog instance);

    int delete(Long id);
}
