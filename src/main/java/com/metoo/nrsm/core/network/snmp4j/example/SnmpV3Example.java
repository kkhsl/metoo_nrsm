package com.metoo.nrsm.core.network.snmp4j.example;


import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.Test;
import org.snmp4j.security.SecurityLevel;

import java.io.IOException;

// 测试通过snmp获取
@Slf4j
public class SnmpV3Example {


    @Test
    public void getArpV6() {
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.0.40")
                .port(161)
                .community("transfar@123")
                .build();

        JSONArray result = SNMPv3Request.getMac(snmpv3Params);
        log.info("arpV6:{}", result);

//        String data = "{\"00:0c:29:1f:18:a7\":\"3\",\"00:0c:29:a4:ab:09\":\"3\",\"00:0c:29:c1:4e:0f\":\"3\",\"00:0c:29:2b:15:43\":\"1\",\"00:0c:29:1b:f2:5c\":\"1\",\"30:80:9b:5a:92:4f\":\"28\",\"00:0c:29:88:d0:2a\":\"3\",\"00:0c:29:e2:1c:2b\":\"3\",\"00:0c:29:1a:2d:00\":\"4\",\"00:0c:29:39:2c:12\":\"4\",\"00:0c:29:54:80:f4\":\"4\",\"00:14:69:ae:2e:58\":\"28\",\"00:0c:29:69:43:47\":\"1\",\"00:0c:29:76:34:8c\":\"4\",\"00:0c:29:aa:8d:fa\":\"4\",\"00:0c:29:bc:09:f3\":\"4\",\"00:0c:29:44:b2:01\":\"3\",\"00:0c:29:cb:81:1c\":\"3\",\"34:73:5a:9d:51:bf\":\"4\",\"00:0c:29:02:b6:50\":\"4\",\"00:0c:29:33:2a:e7\":\"3\",\"00:0c:29:6f:0c:f2\":\"3\",\"00:12:7f:23:91:18\":\"28\",\"5c:c9:99:59:03:69\":\"28\",\"00:0c:29:59:7c:be\":\"4\",\"52:54:00:6d:b0:a4\":\"2\",\"00:0c:29:b1:fc:34\":\"3\",\"38:68:dd:3a:4a:0a\":\"3\",\"00:0c:29:87:2f:cb\":\"4\",\"18:ef:63:23:90:32\":\"28\",\"34:b3:54:10:8d:70\":\"28\",\"00:0c:29:c2:3e:4b\":\"4\",\"00:0c:29:04:b6:1b\":\"4\",\"30:5f:77:a1:b8:73\":\"28\",\"00:0c:29:5c:97:6a\":\"3\",\"00:0c:29:d6:a1:08\":\"4\",\"00:0f:e2:07:f2:e0\":\"28\",\"34:b3:54:10:8d:30\":\"28\",\"00:24:13:ec:be:c0\":\"28\",\"00:0c:29:66:3f:a1\":\"3\",\"2c:fd:a1:8c:87:80\":\"2\",\"00:0c:29:8c:36:81\":\"3\",\"74:25:8a:fa:f2:1c\":\"28\",\"00:0c:29:b5:cb:ba\":\"1\",\"2c:97:b1:72:77:3c\":\"1\",\"00:0c:29:ef:74:e3\":\"1\",\"00:0c:29:18:bb:48\":\"4\",\"00:0c:29:c3:70:43\":\"3\",\"00:24:13:ec:be:b2\":\"28\",\"00:0b:5f:e4:d6:18\":\"28\",\"00:0c:29:7c:73:e2\":\"4\",\"00:0c:29:be:a1:92\":\"4\",\"00:0c:29:1b:27:55\":\"4\",\"74:5a:aa:49:f8:19\":\"2\",\"74:85:c4:37:80:67\":\"28\",\"00:0c:29:5c:d8:a7\":\"3\",\"00:0c:29:e7:19:7b\":\"4\",\"52:54:00:2e:20:fb\":\"2\",\"74:85:c4:37:c9:87\":\"28\",\"52:54:00:e9:bd:05\":\"2\",\"00:0c:29:04:9e:ef\":\"4\",\"00:0c:29:b6:14:da\":\"3\",\"00:0c:29:b3:20:cf\":\"1\",\"34:73:5a:9d:da:4c\":\"10\",\"00:0c:29:cd:77:4e\":\"4\",\"00:0c:29:d0:72:60\":\"1\",\"5c:dd:70:ee:30:56\":\"28\",\"00:0c:29:04:93:4a\":\"4\",\"00:0c:29:76:95:32\":\"1\",\"00:24:f9:fc:fb:32\":\"28\"}";
//        JSONObject jsonObject = new JSONObject(data);
//        for (String mac : jsonObject.keySet()) {
//            // 端口处理
//            String portNumber = jsonObject.optString(mac, "0");
//            System.out.println(portNumber);
//        }
    }

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
        SNMPV3Params v2c = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.6.1")
                .port(161)
                .community("public@123")
                .build();

        String deviceName1 = SNMPv3Request.getDeviceName(v2c);
        System.out.println(deviceName1);
        log.info("主机名 v3：" + deviceName1);


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
        System.out.println(deviceName3);
    }


}