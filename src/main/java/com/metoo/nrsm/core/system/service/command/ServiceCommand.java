package com.metoo.nrsm.core.system.service.command;

import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;

/**
 * 服务命令接口
 * 使用命令模式封装各种服务管理操作
 */
public interface ServiceCommand {
    /**
     * 执行命令
     *
     * @return 命令执行后的服务状态信息
     * @throws ServiceOperationException 当命令执行失败时抛出
     */
    ServiceInfo execute() throws ServiceOperationException;

    /**
     * 获取命令名称
     *
     * @return 命令名称（如status/start等）
     */
    String getCommandName();

    /**
     * 获取服务名称
     *
     * @return 要操作的服务名称
     */
    String getServiceName();
}