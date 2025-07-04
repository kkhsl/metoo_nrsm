package com.metoo.nrsm.core.network.snmp4j.request;

import com.metoo.nrsm.core.vo.Result;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

public class SNMPUtils {
    private final Snmp snmp;
    private final Target target;

    public SNMPUtils(Builder builder) throws IOException {
        // 初始化SNMP实例
        TransportMapping transport = new DefaultUdpTransportMapping();
        this.snmp = new Snmp(transport);

        // 配置安全参数
        if (builder.version == SnmpConstants.version3) {
            configureV3Security(builder);
        }

        transport.listen();
        this.target = createTarget(builder);
    }

    private void configureV3Security(Builder builder) {
        USM usm = new USM(SecurityProtocols.getInstance(),
                new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);

        UsmUser user = createUsmUser(builder);
        snmp.getUSM().addUser(user.getSecurityName(), user);
    }

    private UsmUser createUsmUser(Builder builder) {
        return new UsmUser(
                new OctetString(builder.securityName),
                resolveAuthProtocol(builder.authProtocol),
                // 处理可能的空密码（根据安全级别）
                builder.authPassword != null ?
                        new OctetString(builder.authPassword) :
                        new OctetString(),
                resolvePrivProtocol(builder.privProtocol),
                builder.privPassword != null ?
                        new OctetString(builder.privPassword) :
                        new OctetString()
        );
    }

    private Target createTarget(Builder builder) {
        return (builder.version == SnmpConstants.version3) ?
                createV3Target(builder) : createV2Target(builder);
    }

    private Target createV3Target(Builder builder) {
        UserTarget target = new UserTarget();
        target.setAddress(GenericAddress.parse("udp:" + builder.host + "/161"));
        target.setSecurityLevel(builder.securityLevel);
        target.setSecurityName(new OctetString(builder.securityName));
        target.setTimeout(3000);
        return target;
    }

    private Target createV2Target(Builder builder) {
        CommunityTarget target = new CommunityTarget();
        target.setAddress(GenericAddress.parse("udp:" + builder.host + "/161"));
        target.setCommunity(new OctetString(builder.community));
        target.setTimeout(3000);
        return target;
    }

    public Result snmpGet(String oid) {
        try {
            PDU pdu = createPDU(oid, PDU.GET);
            ResponseEvent event = snmp.send(pdu, target);
            return processResponse(event);
        } catch (IOException e) {
            return new Result(500, null, "SNMP请求失败: " + e.getMessage());
        }
    }

    private PDU createPDU(String oid, int type) {
        PDU pdu = (target instanceof UserTarget) ?
                new ScopedPDU() : new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(type);
        return pdu;
    }

    private Result processResponse(ResponseEvent event) {
        if (event.getResponse() == null) {
            return new Result(404, "无响应", null);
        }

        PDU response = event.getResponse();
        if (response.getErrorStatus() != PDU.noError) {
            return new Result(400, null, response.getErrorStatusText());
        }

        return new Result(200, "成功", response.getVariableBindings());
    }

    public void close() throws IOException {
        if (snmp != null) {
            snmp.close();
        }
    }

    // 构建器模式简化配置
    public static class Builder {
        private String host;
        private int version = SnmpConstants.version2c;
        private String community;
        private String securityName;
        private int securityLevel;
        private String authProtocol;
        private String authPassword;
        private String privProtocol;
        private String privPassword;

        public Builder(String host) {
            this.host = host;
        }

        public Builder v2c(String community) {
            this.version = SnmpConstants.version2c;
            this.community = community;
            return this;
        }

        public Builder v3(String securityName) {
            this.version = SnmpConstants.version3;
            this.securityName = securityName;
            this.securityLevel = SecurityLevel.NOAUTH_NOPRIV; // 默认值
            return this;
        }

        public Builder auth(String protocol, String password) {
            // 升级安全级别
            if (this.securityLevel < SecurityLevel.AUTH_NOPRIV) {
                this.securityLevel = SecurityLevel.AUTH_NOPRIV;
            }
            this.authProtocol = protocol;
            this.authPassword = password;
            return this;
        }

        public Builder encrypt(String protocol, String password) {
            // 强制升级到最高安全级别
            this.securityLevel = SecurityLevel.AUTH_PRIV;
            this.privProtocol = protocol;
            this.privPassword = password;
            return this;
        }

        public SNMPUtils build() throws IOException {
            validate();
            return new SNMPUtils(this);
        }

        private void validate() {
            if (version == SnmpConstants.version3) {
                if (securityName == null) {
                    throw new IllegalArgumentException("v3必须设置securityName");
                }
                if (securityLevel >= SecurityLevel.AUTH_NOPRIV && authProtocol == null) {
                    throw new IllegalArgumentException("认证需要指定协议和密码");
                }
                if (securityLevel == SecurityLevel.AUTH_PRIV && privProtocol == null) {
                    throw new IllegalArgumentException("加密需要指定协议和密码");
                }
            }
        }
    }

    // 协议解析方法
    private static OID resolveAuthProtocol(String protocol) {
        if (protocol == null) return null;
        switch (protocol.toUpperCase()) {
            case "SHA":
                return AuthSHA.ID;
            case "MD5":
                return AuthMD5.ID;
            default:
                throw new IllegalArgumentException("不支持的认证协议: " + protocol);
        }
    }

    private static OID resolvePrivProtocol(String protocol) {
        if (protocol == null) return null;
        switch (protocol.toUpperCase()) {
            case "AES":
                return PrivAES128.ID;
            case "DES":
                return PrivDES.ID;
            default:
                throw new IllegalArgumentException("不支持的加密协议: " + protocol);
        }
    }
}