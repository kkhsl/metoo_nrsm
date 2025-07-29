package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.OperationLogDTO;
import com.metoo.nrsm.entity.OperationLog;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2023-11-07 16:12
 */
public interface IOperationLogService {

    OperationLog selectObjById(Long id);

    Page<OperationLog> selectObjConditionQuery(OperationLogDTO instance);

    List<OperationLog> selectObjByMap(Map params);

    boolean save(OperationLog instance);

    boolean saveLoginLog(OperationLog operationLog);
    boolean saveOperationLog(OperationLog operationLog);

    boolean update(OperationLog instance);

    boolean delete(Long id);
}
