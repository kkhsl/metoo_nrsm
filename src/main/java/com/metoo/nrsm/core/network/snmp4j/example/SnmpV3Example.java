package com.metoo.nrsm.core.network.snmp4j.example;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPParamFactory;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.utils.py.ssh.SSHExecutor;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.Port;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.Test;
import org.snmp4j.security.SecurityLevel;

import java.io.IOException;
import java.util.List;

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
                .host("192.168.0.21")
                .port(161)
                .community("hnccsroot_read")
                .build();

        System.out.println(SNMPv3Request.getDeviceName(snmpParams));


//        SNMPv3Request.getDeviceName(SNMPParamFactory.createSNMPParam(networkElement))

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




    // 测试v2版本
    @Test
    public void testSNMPv2(){
        NetworkElement networkElement = new NetworkElement();
        networkElement.setIp("192.168.0.16");
        networkElement.setVersion("v2c");
        networkElement.setCommunity("hnitms_ro");
        SNMPV3Params snmpv3Params = SNMPParamFactory.createSNMPParam(networkElement);
        String deviceName = SNMPv3Request.getDeviceName(snmpv3Params);
        System.out.println(deviceName);
    }


    //  v3 方式一：NOAUTH_NOPRIV（无认证、无加密）
    @Test
    public void testSNMPv31(){
//
//        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
//                .version("v3")
//                .host("192.168.6.1")
//                .port(161)
//                .username("user_test")
//                .securityLevel(SecurityLevel.NOAUTH_NOPRIV)
//                .build();

        NetworkElement networkElement = new NetworkElement();
        networkElement.setIp("192.168.6.1");
        networkElement.setVersion("v3");
        networkElement.setSecurityName("user_test");
        networkElement.setSecurityLevel(1);
        SNMPV3Params snmpv3Params = SNMPParamFactory.createSNMPParam(networkElement);

        String deviceName = SNMPv3Request.getDeviceName(snmpv3Params);
        log.info("方式一{}", deviceName);

    }

    //  v3 方式二：AUTH_NOPRIV（有认证、无加密）
    @Test
    public void testSNMPv32(){
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v3")
                .host("192.168.6.1")
                .port(161)
                .securityLevel(SecurityLevel.AUTH_NOPRIV)
                .username("user-test2")
                .authProtocol("MD5")
                .authPassword("metoo8974500")
                .build();

        String deviceName = SNMPv3Request.getDeviceName(snmpv3Params);
        log.info("方式二{}", deviceName);


    }


    //  v3 方式三：AUTH_PRIV（有认证和加密）
    @Test
    public void testSNMPv3(){
//        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
//                .version("v3")
//                .host("192.168.0.8")
//                .port(161)
//                .securityLevel(SecurityLevel.AUTH_PRIV)
//                .username("TestAbstrack")
//                .authProtocol("SHA")
//                .authPassword("pulic@123")
//                .privProtocol("AES")
//                .privPassword("PUBLIC@123")
//                .build();
//
//        String deviceName = SNMPv3Request.getDeviceName(snmpv3Params);
//        log.info("方式三{}", deviceName);

        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v3")
                .host("192.168.0.8")
                .port(161)
                .securityLevel(SecurityLevel.AUTH_PRIV)
                .username("TestAbstrack")
                .authProtocol("SHA")
                .authPassword("public@123")
                .privProtocol("AES")
                .privPassword("PUBLIC@123")
                .build();

        String deviceName = SNMPv3Request.getDeviceName(snmpv3Params);
        log.info("方式三：{}", deviceName);
    }

    @Test
    public void testSNMPv3GetTraffic(){
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.0.8")
                .community("transfar@123")
                .port(161)
                .build();

        String traffic = SNMPv3Request.getTraffic(snmpv3Params, "1.3.6.1.2.1.31.1.1.1.6.31", "1.3.6.1.2.1.31.1.1.1.10.31");
        log.info("流量数据", traffic);

        String deviceName = SNMPv3Request.getDeviceName(snmpv3Params);
        log.info("设备名：{}", deviceName);


    }

    @Test
    public void getUpTime(){
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.6.1")
                .community("public@123")
                .port(161)
                .build();
        String upTime = SNMPv3Request.getDeviceUpdateTime(snmpv3Params);
        log.info("启动时间：{}", upTime);
    }

    @Test
    public void testSNMPv3GetTrafficPY(){
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {"113.240.243.196", "v2c",
                "transfar@123", "1.3.6.1.2.1.31.1.1.1.6.31", "1.3.6.1.2.1.31.1.1.1.10.31"};

        SSHExecutor sshExecutor = new SSHExecutor("192.168.60.90", 22, "root", "Transfar@123");

        String result = sshExecutor.exec(path, params);

        System.out.println(result);


    }

    @Test
    public void aaa(){
        String a = gettraffic("113.240.243.196", "", "transfar@123", "1.3.6.1.2.1.31.1.1.1.6.31", "1.3.6.1.2.1.31.1.1.1.10.31");
        System.out.println(a);

    }



    public String gettraffic(String ip, String version, String community, String in, String out) {
//        String path = Global.PYPATH + "gettraffic.py";
//        String[] params = {ip, "v2c",
//                community, in, out};
//
//        String result = pythonExecUtils.exec2(path, params);
//        if(StringUtil.isNotEmpty(result)){
//            return result;
//        }
//        return null;

        version = "v2c";

        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version(version)
                .host(ip)
                .version(version)
                .community(community)
                .build();

        String traffic = SNMPv3Request.getTraffic(snmpv3Params, in, out);
        if(StringUtil.isNotEmpty(traffic)){
            return traffic;
        }
        return "";
    }

    @Test
    public void getDeviceName() {
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.0.1")
                .community("transfar@123")
                .port(161)
                .build();
        String result = SNMPv3Request.getDeviceName(snmpv3Params);
        log.info("设备名:{}", result);

        NetworkElement networkElement = new NetworkElement();
        networkElement.setIp("192.168.0.1");
        networkElement.setVersion("v2c");
        networkElement.setCommunity("transfar@123");
        networkElement.setSnmpPort(161);
        String hostname = SNMPv3Request.getDeviceName(SNMPParamFactory.createSNMPParam(networkElement));
        log.info("设备名:{}", hostname);

    }


    @Test
    public void getPortTable() {
        NetworkElement networkElement = new NetworkElement();
        networkElement.setIp("192.168.0.40");
        networkElement.setVersion("v2c");
        networkElement.setCommunity("transfar");
        networkElement.setSnmpPort(161);
        JSONArray result = SNMPv3Request.getPortTable(SNMPParamFactory.createSNMPParam(networkElement));
        if(!result.isEmpty()){
            // 使用 Jackson 将 JSON 字符串转换为
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<Port> ports = objectMapper.readValue(result.toString(), new TypeReference<List<Port>>(){});
                log.info("端口列表:{}", ports);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
    }

    @Test
    public void getArp() {
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.4.1")
                .community("transfar@123")
                .build();
        JSONArray result = SNMPv3Request.getArp(snmpv3Params);
        log.info("arp:{}", result);
    }



    @Test
    public void getDDL() {

        NetworkElement networkElement = new NetworkElement();
        networkElement.setIp("192.168.0.25");
        networkElement.setVersion("v2c");
        networkElement.setCommunity("transfar");
        SNMPV3Params snmpv3Params = SNMPParamFactory.createSNMPParam(networkElement);

        JSONArray result = SNMPv3Request.getLldp(snmpv3Params);
        log.info("getLldp:{}", result);

        result = SNMPv3Request.getMac(SNMPParamFactory.createSNMPParam(networkElement));
        log.info("getMac:{}", result);

        result = SNMPv3Request.getPortMac(SNMPParamFactory.createSNMPParam(networkElement));
        log.info("getPortMac:{}", result);
    }


}