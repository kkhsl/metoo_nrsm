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
        if(networkElement.getSnmpPort() == null || "".equals(networkElement.getSnmpPort())){
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
                    return createV1Param(networkElement.getIp(), networkElement.getSnmpPort(), networkElement.getCommunity(), 1500, 3);
                case "v2c":
                    return createV2cParam(networkElement.getIp(), networkElement.getSnmpPort(), networkElement.getCommunity(), 1500, 3);
                case "v3":
                    if (networkElement.getSecurityLevel() == null) {
                        throw new IllegalArgumentException("SNMPv3 requires security level");
                    }
                    return createV3Param(networkElement.getIp(), networkElement.getSnmpPort(), networkElement.getSecurityName(), networkElement.getAuthProtocol(), networkElement.getAuthPassword(),
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

}
