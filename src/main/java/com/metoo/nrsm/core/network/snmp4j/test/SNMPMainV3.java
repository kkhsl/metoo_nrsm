package com.metoo.nrsm.core.network.snmp4j.test;

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

import java.util.List;

@Slf4j
public class SNMPMainV3 {

    //  v3 方式一：NOAUTH_NOPRIV（无认证、无加密）
    @Test
    public void testSNMPv31() {
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
    public void testSNMPv32() {
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
    public void testSNMPv3() {
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
    public void testSNMPv3GetTraffic() {
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
    public void getUpTime() {
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
    public void testSNMPv3GetTrafficPY() {
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {"113.240.243.196", "v2c",
                "transfar@123", "1.3.6.1.2.1.31.1.1.1.6.31", "1.3.6.1.2.1.31.1.1.1.10.31"};

        SSHExecutor sshExecutor = new SSHExecutor("192.168.60.90", 22, "root", "Transfar@123");

        String result = sshExecutor.exec(path, params);

        System.out.println(result);


    }

    @Test
    public void aaa() {
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
        if (StringUtil.isNotEmpty(traffic)) {
            return traffic;
        }
        return "";
    }

    @Test
    public void getDeviceName() {
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("172.16.253.254")
                .community("transfar@123")
                .port(161)
                .build();
        String result = SNMPv3Request.getDeviceName(snmpv3Params);
        log.info("设备名:{}", result);

//        NetworkElement networkElement = new NetworkElement();
//        networkElement.setIp("172.16.253.254");
//        networkElement.setVersion("v2c");
//        networkElement.setCommunity("public@123");
//        networkElement.setSnmpPort(161);
//        String hostname = SNMPv3Request.getDeviceName(SNMPParamFactory.createSNMPParam(networkElement));
//        log.info("设备名:{}", hostname);

    }


    @Test
    public void getPortTable() {
        NetworkElement networkElement = new NetworkElement();
        networkElement.setIp("172.16.253.254");
        networkElement.setVersion("v2c");
        networkElement.setCommunity("transfar@123");
        networkElement.setSnmpPort(161);
        JSONArray result = SNMPv3Request.getPortTable(SNMPParamFactory.createSNMPParam(networkElement));
        if (!result.isEmpty()) {
            // 使用 Jackson 将 JSON 字符串转换为
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<Port> ports = objectMapper.readValue(result.toString(), new TypeReference<List<Port>>() {
                });
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
