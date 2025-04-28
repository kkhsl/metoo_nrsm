package com.metoo.nrsm.core.network.snmp4j.request;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.SSHExecutor;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.Test;
import org.snmp4j.security.SecurityLevel;

@Slf4j
public class SNMPParamFactory {

    public static SNMPV3Params createSNMPParam(NetworkElement networkElement) {
        if(networkElement.getVersion() == null || "".equals(networkElement.getVersion())){
            log.info("version is null");
            return null;
        }
        if(networkElement.getPort() == null || "".equals(networkElement.getPort())){
            log.info("port is null");
            return null;
        }
        if(networkElement.getCommunity() == null || "".equals(networkElement.getCommunity())){
            log.info("community is null");
            return null;
        }
        try {
            switch(networkElement.getVersion()) {
                case "v1":
                    return createV1Param(networkElement.getIp(), networkElement.getPort(), networkElement.getCommunity(), 1500, 3);
                case "v2c":
                    return createV2cParam(networkElement.getIp(), networkElement.getPort(), networkElement.getCommunity(), 1500, 3);
                case "v3":
                    if (networkElement.getSecurityLevel() == null) {
                        throw new IllegalArgumentException("SNMPv3 requires security level");
                    }
                    return createV3Param(networkElement.getIp(), networkElement.getPort(), networkElement.getSecurityName(), networkElement.getAuthProtocol(), networkElement.getAuthPassword(),
                            networkElement.getPrivProtocol(), networkElement.getPrivPassword(), 1500, 3,
                            networkElement.getSecurityLevel());

                default:
                    throw new IllegalArgumentException("Unsupported SNMP version: " + networkElement.getVersion());
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * v1
     * @param host
     * @param community
     * @param timeout
     * @param retries
     * @return
     */
    private static SNMPV3Params createV1Param(String host, int port, String community,
                                           int timeout, int retries) {
        if (community == null || community.isEmpty()) {
            throw new IllegalArgumentException("SNMPv1 requires community string");
        }

        return new SNMPV3Params.Builder()
                .version("v1")
                .host(host)
                .port(port)
                .community(community)
                .timeout(timeout)
                .retries(retries)
                .build();
    }


    /**
     * v2c
     * @param host
     * @param port
     * @param community
     * @param timeout
     * @param retries
     * @return
     */
    private static SNMPV3Params createV2cParam(String host, int port, String community,
                                            int timeout, int retries) {
        if (community == null || community.isEmpty()) {
            throw new IllegalArgumentException("SNMPv2c requires community string");
        }

        return new SNMPV3Params.Builder()
                .version("v2c")
                .host(host)
                .port(port)
                .community(community)
                .timeout(timeout)
                .retries(retries)
                .build();
    }

    /**
     * v3
     * @param host
     * @param port
     * @param username
     * @param authProtocol
     * @param authPassword
     * @param privProtocol
     * @param privPassword
     * @param timeout
     * @param retries
     * @param securityLevel
     * @return
     */
    private static SNMPV3Params createV3Param(String host, int port, String username,
                                           String authProtocol, String authPassword,
                                           String privProtocol, String privPassword,
                                           int timeout, int retries,
                                           int securityLevel) {
        // 验证基本参数
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("SNMPv3 requires username");
        }

        // 根据安全级别验证参数
        switch (securityLevel) {
            case 1: // noAuthNoPriv
                break;

            case 2: // authNoPriv
                if (authProtocol == null || authPassword == null) {
                    throw new IllegalArgumentException("AuthNoPriv level requires auth protocol and password");
                }
                break;

            case 3: // authPriv
                if (authProtocol == null || authPassword == null ||
                        privProtocol == null || privPassword == null) {
                    throw new IllegalArgumentException("AuthPriv level requires all security parameters");
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid SNMPv3 security level: " + securityLevel);
        }

        return new SNMPV3Params.Builder()
                .version("v3")
                .host(host)
                .port(port)
                .username(username)
                .securityLevel(securityLevel)
                .authProtocol(authProtocol)
                .authPassword(authPassword)
                .privProtocol(privProtocol)
                .privPassword(privPassword)
                .timeout(timeout)
                .retries(retries)
                .build();
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
//                .username("test")
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
                .username("test")
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
                .host("192.168.0.25")
                .community("transfar")
                .port(161)
                .build();

        String result = SNMPv3Request.getDeviceName(snmpv3Params);
        log.info("设备名:{}", result);
    }

    @Test
    public void getMac() {
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.0.13")
                .community("transfar")
                .port(161)
                .build();

       JSONArray result = SNMPv3Request.getMac(snmpv3Params);
       log.info("arpV6:{}", result);
    }

    @Test
    public void getArp() {
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.0.36")
                .community("hnccsroot_read")
                .build();
        JSONArray result = SNMPv3Request.getArp(snmpv3Params);
        log.info("arpV6:{}", result);
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
