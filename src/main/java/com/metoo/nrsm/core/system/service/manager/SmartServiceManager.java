package com.metoo.nrsm.core.system.service.manager;


import com.metoo.nrsm.core.system.service.command.factory.impl.SystemctlCommandFactory;
import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.manager.impl.LinuxServiceManager;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;
import com.metoo.nrsm.core.system.service.remote.RemoteServiceManager;

/**
 * 智能服务管理器
 * 根据主机地址自动判断使用本地还是远程执行
 */
public class SmartServiceManager implements ServiceManager {

    private final ServiceManager delegate;  // 实际执行的服务管理器

    /**
     * 构造方法
     * @param host 主机地址(localhost/127.0.0.1表示本地)
     * @param port SSH端口(本地执行时忽略)
     * @param username SSH用户名(本地执行时忽略)
     * @param password SSH密码(本地执行时忽略)
     * @param timeout 超时时间(毫秒)
     */
    public SmartServiceManager(String host, int port, String username,
                               String password, int timeout) {
        // 远程执行 - 使用RemoteServiceManager
        this.delegate = new RemoteServiceManager(host, port, username, password, timeout);
    }


    /**
     * 本地执行时的构造方法
     */
    public SmartServiceManager() {
        this.delegate = new LinuxServiceManager(new SystemctlCommandFactory());
    }

//    /**
//     * 私有构造函数
//     * @param delegate 服务管理器的实现
//     */
//    private SmartServiceManager(ServiceManager delegate) {
//        this.delegate = delegate;
//    }
//
//    // 通过工厂方法创建 SmartServiceManager 实例
//    public static SmartServiceManager createLocalServiceManager() {
//        // 本地执行 - 使用 LinuxServiceManager
//        ServiceManager delegate1 = new LinuxServiceManager(new SystemctlCommandFactory());
//        return new SmartServiceManager(delegate1);
//    }
//
//    public static SmartServiceManager createRemoteServiceManager(String host, int port, String username,
//                                                                 String password, int timeout) {
//        // 远程执行 - 使用 RemoteServiceManager
//        ServiceManager delegate1 = new RemoteServiceManager(host, port, username, password, timeout);
//        return new SmartServiceManager(delegate1);
//    }

    /**
     * 判断是否为本地主机
     * @param host 主机地址
     * @return 如果是本地返回true，否则false
     */
    private boolean isLocalHost(String host) {
        return "localhost".equalsIgnoreCase(host) ||
                "127.0.0.1".equals(host) ||
                "::1".equals(host);
    }

    @Override
    public ServiceInfo getStatus(String serviceName) throws ServiceOperationException {
        return delegate.getStatus(serviceName);
    }

    @Override
    public ServiceInfo startService(String serviceName) throws ServiceOperationException {
        return delegate.startService(serviceName);
    }

    @Override
    public ServiceInfo stopService(String serviceName) throws ServiceOperationException {
        return delegate.stopService(serviceName);
    }

    @Override
    public ServiceInfo restartService(String serviceName) throws ServiceOperationException {
        return delegate.restartService(serviceName);
    }

    @Override
    public ServiceInfo enableService(String serviceName) throws ServiceOperationException {
        return delegate.enableService(serviceName);
    }

    @Override
    public ServiceInfo disableService(String serviceName) throws ServiceOperationException {
        return delegate.disableService(serviceName);
    }
}