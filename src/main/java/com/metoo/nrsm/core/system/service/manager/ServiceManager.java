package com.metoo.nrsm.core.system.service.manager;

import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;
/**
 * 服务管理器接口
 * 提供对系统服务的高级管理功能
 */
public interface ServiceManager {

    ServiceInfo getStatus(String serviceName) throws ServiceOperationException;
    ServiceInfo startService(String serviceName) throws ServiceOperationException;
    ServiceInfo stopService(String serviceName) throws ServiceOperationException;
    ServiceInfo restartService(String serviceName) throws ServiceOperationException;
    ServiceInfo enableService(String serviceName) throws ServiceOperationException;
    ServiceInfo disableService(String serviceName) throws ServiceOperationException;
}
