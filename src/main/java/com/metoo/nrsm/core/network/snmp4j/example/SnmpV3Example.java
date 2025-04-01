package com.metoo.nrsm.core.network.snmp4j.example;


import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPV3Request;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;

import java.io.IOException;

// 测试通过snmp获取
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
        SNMPV3Params v3Params1 = new SNMPV3Params();
        v3Params1.setIp("192.168.6.1");
        v3Params1.setVersion(SnmpConstants.version2c);
        v3Params1.setCommunity("public@123");
        v3Params1.validate();

        String deviceName1 = SNMPV3Request.getDeviceName(v3Params1);
        System.out.println(deviceName1);


        // v3带认证加密的请求
        SNMPV3Params v3Params = new SNMPV3Params();
        v3Params.setIp("192.168.6.1");
        v3Params.setVersion(SnmpConstants.version3);
        v3Params.setSecurityName("user_test");
        v3Params.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
        v3Params.validate();

        String deviceName = SNMPV3Request.getDeviceName(v3Params);
        System.out.println(deviceName);




        SNMPV3Params v3Params2 = new SNMPV3Params();
        v3Params2.setIp("192.168.6.1");
        v3Params2.setVersion(SnmpConstants.version3);
        v3Params2.setSecurityName("user-test2");
        v3Params2.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
        v3Params2.setAuthProtocol("MD5");
        v3Params2.setAuthPassword("metoo8974500");
        v3Params2.validate();

        String deviceName2 = SNMPV3Request.getDeviceName(v3Params2);
        System.out.println(deviceName2);



        SNMPV3Params v3Params3 = new SNMPV3Params();
        v3Params3.setIp("192.168.6.1");
        v3Params3.setVersion(SnmpConstants.version3);
        v3Params3.setSecurityName("user_test3");
        v3Params3.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        v3Params3.setAuthProtocol("MD5");
        v3Params3.setAuthPassword("metoo8974500");
        v3Params3.setPrivProtocol("DES");
        v3Params3.setPrivPassword("Metoo89745000");
        v3Params3.validate();

        String deviceName3 = SNMPV3Request.getDeviceName(v3Params3);
        System.out.println(deviceName3);
    }
}