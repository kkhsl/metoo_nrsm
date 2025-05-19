package com.metoo.nrsm.core.system.service.command.impl;

import com.metoo.nrsm.core.system.service.command.ServiceCommand;
import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;
import com.metoo.nrsm.core.system.service.utils.ServiceInfoParser;

import java.io.IOException;
/**
 * systemctl命令的基类
 * 封装了执行systemctl命令的通用逻辑
 */
public abstract class BaseSystemctlCommand implements ServiceCommand {
    protected final String serviceName;  // 服务名称
    protected final ServiceInfoParser parser;  // 输出解析器

    public BaseSystemctlCommand(String serviceName) {
        this.serviceName = serviceName;
        this.parser = new ServiceInfoParser();
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 执行systemctl命令的通用方法
     * @param args 命令参数（不包含systemctl和服务名）
     * @return 命令执行结果
     * @throws ServiceOperationException 当命令执行失败时抛出
     */
    protected ServiceInfo executeCommand(String... args) throws ServiceOperationException {
        try {
            // 构建完整命令数组：[systemctl, 命令, 服务名]
            String[] fullCommand = new String[args.length + 2];
            fullCommand[0] = "systemctl";
            System.arraycopy(args, 0, fullCommand, 1, args.length);
            fullCommand[fullCommand.length - 1] = serviceName;

            // 执行命令并等待完成
            Process process = new ProcessBuilder(fullCommand)
                    .redirectErrorStream(true)  // 合并错误流到标准输出
                    .start();

            process.waitFor();

            // 对于status命令，直接解析输出
            if ("status".equals(getCommandName())) {
                return parser.parse(process.getInputStream());
            }

            // 对于其他命令，执行后再次获取状态
            return new SystemctlStatusCommand(serviceName).execute();

        } catch (IOException | InterruptedException e) {
            throw new ServiceOperationException(
                    String.format("执行%s命令失败[服务:%s]", getCommandName(), serviceName), e);
        }
    }
}
