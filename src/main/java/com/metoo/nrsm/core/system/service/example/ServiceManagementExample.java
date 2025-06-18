package com.metoo.nrsm.core.system.service.example;

import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;
import com.metoo.nrsm.core.system.service.manager.SmartServiceManager;
import org.junit.Test;

/**
 * 架构设计：
 *
 * 采用命令模式封装服务操作
 *
 * 使用工厂模式创建命令对象
 *
 * 策略模式实现本地/远程执行自动切换
 *
 * 扩展性：
 *
 * 易于添加新的服务操作命令
 *
 * 支持扩展其他远程协议(如WinRM)
 *
 * 可以轻松替换命令执行方式
 *
 * 异常处理：
 *
 * 自定义异常清晰区分不同错误类型
 *
 * 统一异常处理接口
 *
 * 测试友好：
 *
 * 本地执行模式便于单元测试
 *
 * 清晰的接口定义方便mock测试
 */

/**
 * 服务管理使用示例
 */
public class ServiceManagementExample {

    public static void main(String[] args) {
        // 配置参数
        String host = "192.168.6.102"; // 或远程主机IP "192.168.1.100"
        int port = 22;            // SSH端口
        String username = "root"; // SSH用户名
        String password = "Metoo89745000!"; // SSH密码
        int timeout = 5000;        // 超时时间(毫秒)

        String serviceName = "radvd"; // 要管理的服务名

        try {
            // 1. 创建服务管理器
            SmartServiceManager serviceManager = new SmartServiceManager(
                    host, port, username, password, timeout);

            // 2. 获取服务状态
            System.out.println("=== 获取服务状态 ===");
            ServiceInfo status = serviceManager.getStatus(serviceName);
            System.out.println(status);

            // 3. 重启服务
            System.out.println("\n=== 重启服务 ===");
            ServiceInfo restarted = serviceManager.restartService(serviceName);
            System.out.println("重启结果: " + restarted);

            // 4. 启用开机自启(如果未启用)
            if (!restarted.isEnabled()) {
                System.out.println("\n=== 启用开机自启 ===");
                ServiceInfo enabled = serviceManager.enableService(serviceName);
                System.out.println("启用结果: " + enabled);
            }

            // 5. 最终状态
            System.out.println("\n=== 最终状态 ===");
            ServiceInfo finalStatus = serviceManager.getStatus(serviceName);
            System.out.println(finalStatus);

        } catch (ServiceOperationException e) {
            System.err.println("服务操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void status(){
        // 配置参数
        String host = "192.168.6.102"; // 或远程主机IP "192.168.1.100"
        int port = 22;            // SSH端口
        String username = "root"; // SSH用户名
        String password = "Metoo89745000!"; // SSH密码
        int timeout = 5000;        // 超时时间(毫秒)

        String serviceName = "radvd"; // 要管理的服务名

        try {
            // 1. 创建服务管理器
            SmartServiceManager serviceManager = new SmartServiceManager(
                    host, port, username, password, timeout);

            System.out.println("=== 获取服务状态 ===");
            ServiceInfo status = serviceManager.getStatus(serviceName);
            System.out.println(status);

        } catch (ServiceOperationException e) {
            System.err.println("服务操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}