package com.metoo.nrsm.core.network.snmp4j.example;


import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

// 测试通过snmp获取
@Slf4j
public class SnmpV3Example {

    public static void main(String[] args) throws IOException {
/*

        // v2c示例
        SNMPUtils v2Manager = new SNMPUtils.Builder("192.168.6.1")
                .v2c("public@123")
                .build();
        Result result = v2Manager.snmpGet("1.3.6.1.2.1.4.22.1.2");
        System.out.println(result.getData());

        try {
            // v3示例
            // 示例1：自动推断为 NOAUTH_NOPRIV
            SNMPUtils manager1 = new SNMPUtils.Builder("192.168.6.1")
                    .v3("user_test")
                    .build();
            Result result1 = manager1.snmpGet("1.3.6.1.2.1.1.5.0");
            System.out.println(result1.getData());

            // 示例2：自动升级到 AUTH_NOPRIV
            SNMPUtils manager2 = new SNMPUtils.Builder("192.168.6.1")
                    .v3("user-test2")
                    .auth("MD5", "metoo8974500")
                    .build();
            Result result2 = manager2.snmpGet("1.3.6.1.2.1.1.5.0");
            System.out.println(result2.getData());

            // 示例3：自动升级到 AUTH_PRIV
            SNMPUtils manager3 = new SNMPUtils.Builder("192.168.6.1")
                    .v3("user_test3")
                    .auth("MD5", "metoo8974500")
                    .encrypt("DES", "Metoo89745000")
                    .build();
            Result result3 = manager3.snmpGet("1.3.6.1.2.1.1.5.0");
            System.out.println(result3.getData());

        } catch (Exception e) {
            e.printStackTrace();
        }

*/

        // v2
//        SNMPV3Params v3Params1 = new SNMPV3Params();
//        v3Params1.setIp("192.168.6.1");
//        v3Params1.setVersion("v2c");
//        v3Params1.setCommunity("public@123");
//        v3Params1.validate();
//        v3Params1.setSecurityName(null);
//        v3Params1.setSecurityLevel(0);
//        v3Params1.validate();

        // 调整
        SNMPV3Params snmpParams = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.204.1")
                .port(161)
                .community("public@123")
                .build();

        System.out.println(SNMPv3Request.getRoute(snmpParams));

/*
        // v3带认证加密的请求
        // 方式一
//        SNMPV3Params v3Params = new SNMPV3Params();
//        v3Params.setIp("192.168.6.1");
////        v3Params.setVersion(SnmpConstants.version3);
//
//        v3Params.setVersion("v3");
//
//        v3Params.setSecurityName("user_test");
//        v3Params.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
//
//        v3Params.validate();

        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v3")
                .host("192.168.6.1")
                .port(161)
                .username("user_test")
                .securityLevel(SecurityLevel.NOAUTH_NOPRIV)
                .build();

        String deviceName = SNMPv3Request.getDeviceName(snmpv3Params);
        System.out.println(deviceName);
        log.info("主机名 无认证：" + deviceName1);



        // 方式二
//        SNMPV3Params v3Params2 = new SNMPV3Params();
//        v3Params2.setIp("192.168.6.1");
////        v3Params2.setVersion(SnmpConstants.version3);
//        v3Params.setVersion("v3");
//
//        v3Params2.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
//        v3Params2.setSecurityName("user-test2");
//        v3Params2.setAuthProtocol("MD5");// SHA
//        v3Params2.setAuthPassword("metoo8974500");
//        v3Params2.validate();

        snmpv3Params = new SNMPV3Params.Builder()
                .version("v3")
                .host("192.168.6.1")
                .port(161)
                .username("user_test")
                .securityLevel(SecurityLevel.AUTH_NOPRIV)
                .username("user-test2")
                .authProtocol("MD5")
                .authPassword("metoo8974500")
                .build();

        String deviceName2 = SNMPv3Request.getDeviceName(snmpv3Params);
        System.out.println(deviceName2);



        // 方式三
//        SNMPV3Params v3Params3 = new SNMPV3Params();
//        v3Params3.setIp("192.168.6.1");
////        v3Params3.setVersion(SnmpConstants.version3);
//
//        v3Params.setVersion("v3");
//
//        v3Params3.setSecurityLevel(SecurityLevel.AUTH_PRIV);
//        v3Params3.setSecurityName("user_test3");
//        v3Params3.setAuthProtocol("MD5");
//        v3Params3.setAuthPassword("metoo8974500");
//        v3Params3.setPrivProtocol("DES");
//        v3Params3.setPrivPassword("Metoo89745000");
//        v3Params3.validate();

        snmpv3Params = new SNMPV3Params.Builder()
                .version("v3")
                .host("192.168.6.1")
                .port(161)
                .securityLevel(SecurityLevel.AUTH_PRIV)
                .username("user-test3")
                .authProtocol("MD5")
                .authPassword("metoo8974500")
                .privProtocol("DES")
                .privPassword("Metoo89745000")
                .build();

        String deviceName3 = SNMPv3Request.getDeviceName(snmpv3Params);
        System.out.println(deviceName3);*/
    }


}