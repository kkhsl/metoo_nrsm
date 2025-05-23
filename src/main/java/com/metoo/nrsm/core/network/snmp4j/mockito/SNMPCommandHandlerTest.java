package com.metoo.nrsm.core.network.snmp4j.mockito;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // 使用 MockitoExtension 扩展
public class SNMPCommandHandlerTest {

    @InjectMocks
    private SNMPCommandHandler snmpCommandHandler; // 需要测试的类

    @Mock
    private SNMPClient snmpClient; // 模拟的 SNMPClient

    // 在每次测试前初始化 Mockito 注解，MockitoExtension 会自动处理
    @BeforeEach
    void setUp() {
        // Mockito 注解会自动初始化所有的 Mock 和 InjectMocks
    }




    @Test
    public void snmpCommandHandler() throws Exception {
        // 模拟配置文件中的方法
        String vendor = "h3c";
        String command = "TestAbstrack";
        // 调用需要测试的方法
        SNMPCommandHandler snmpCommandHandler = new SNMPCommandHandler();
        snmpCommandHandler.handleCommand(vendor, command, "192.168.1.1", "2c", "public", "1.3.6.1.2.1.1");
    }

    @Test
    public void testHandleCommand_H3C_Test() throws Exception {
        // 模拟配置文件中的方法
        String vendor = "h3c";
        String command = "TestAbstrack";

        // 假设 getHostname 返回 "设备 xxx 的主机名"
        when(snmpClient.getHostname(any(), any(), any(), any())).thenReturn("设备 xxx 的主机名");

        // 调用需要测试的方法
        snmpCommandHandler.handleCommand(vendor, command, "192.168.1.1", "2c", "public", "1.3.6.1.2.1.1");

        // 验证 snmpClient 的 getHostname 方法是否被正确调用
        verify(snmpClient).getHostname("192.168.1.1", "2c", "public", "1.3.6.1.2.1.1");
    }
}