package com.metoo.nrsm.core.network;

import com.metoo.nrsm.core.network.jopo.SnmpWalkResult;

public class SnmpWalkExecutor {


    public static void main(String[] args) {
        // 假设从 properties 文件中读取配置
        String executionType = getExecutionTypeFromConfig(); // 读取配置，"ssh" 或 "local"

        SnmpWalkContext context = new SnmpWalkContext(executionType);

        // 假设有一个 NetworkElement 对象和 SnmpWalkResult
        SnmpWalkResult snmpWalkResult = new SnmpWalkResult()
                .setIp("192.168.6.1")
                .setVersion("v2c")
                .setCommunity("public@123");

        context.execute(snmpWalkResult);

        System.out.println("Host Name: " + snmpWalkResult.getResult());
    }

    private static String getExecutionTypeFromConfig() {
        // 假设从 properties 文件中读取配置，"ssh" 或 "local"
        // 在实际应用中可以使用 Properties 类来读取配置文件
        return "dev"; // 示例，实际可能是通过读取配置文件或环境变量来动态决定
    }
}
