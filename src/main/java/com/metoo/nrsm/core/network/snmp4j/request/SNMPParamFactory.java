package com.metoo.nrsm.core.network.snmp4j.request;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.snmp4j.security.SecurityLevel;

@Slf4j
public class SNMPParamFactory {

    public static SNMPV3Params createSNMPParam(NetworkElement networkElement) {
        switch(networkElement.getVersion()) {
            case "v1":
                return createV1Param(networkElement.getIp(), 161, networkElement.getCommunity(), 1500, 3);
            case "v2c":
                return createV2cParam(networkElement.getIp(), 161, networkElement.getCommunity(), 1500, 3);

            case "v3":
                if (networkElement.getSecurityLevel() == null) {
                    throw new IllegalArgumentException("SNMPv3 requires security level");
                }
                return createV3Param(networkElement.getIp(), 161, networkElement.getSecurityName(), networkElement.getAuthProtocol(), networkElement.getAuthPassword(),
                        networkElement.getPrivProtocol(), networkElement.getPrivPassword(), 1500, 3,
                        networkElement.getSecurityLevel());

            default:
                throw new IllegalArgumentException("Unsupported SNMP version: " + networkElement.getVersion());
        }
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
        networkElement.setIp("192.168.6.1");
        networkElement.setVersion("v2c");
        networkElement.setCommunity("public@123");
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
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
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

        String deviceName = SNMPv3Request.getDeviceName(snmpv3Params);
        log.info("方式三{}", deviceName);
    }

}
