package com.metoo.nrsm.core.network.snmp4j.param;

import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;

public class SNMPV3Params {
    private String ip;
    private int version;            //版本     1：v2c   3:v3
    private String community;       // v2c参数

    // v3参数
    private String securityName;    //  安全名
    private int securityLevel;    // 1:NOAUTH_NOPRIV  2:AUTH_NOPRIV  3:AUTH_PRIV
    private String authProtocol;  //认证协议  "MD5"和"SHA"
    private String authPassword;   //认证密码
    private String privProtocol;   //加密协议   "AES"和"DES"
    private String privPassword;   //加密密码
    private int timeout = 5000;
    private int retries = 3;



    // 新增验证方法
    public void validate() {
        if (version == SnmpConstants.version3) {
            if (securityName == null) {
                throw new IllegalArgumentException("v3必须设置securityName");
            }
            if (securityLevel >= SecurityLevel.AUTH_NOPRIV &&
                    (authProtocol == null || authPassword == null)) {
                throw new IllegalArgumentException("认证需要完整参数");
            }
            if (securityLevel == SecurityLevel.AUTH_PRIV &&
                    (privProtocol == null || privPassword == null)) {
                throw new IllegalArgumentException("加密需要完整参数");
            }
        }
    }


    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }
    public String getSecurityName() { return securityName; }
    public void setSecurityName(String securityName) { this.securityName = securityName; }
    public int getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(int securityLevel) { this.securityLevel = securityLevel; }
    public String getAuthProtocol() { return authProtocol; }
    public void setAuthProtocol(String authProtocol) { this.authProtocol = authProtocol; }
    public String getAuthPassword() { return authPassword; }
    public void setAuthPassword(String authPassword) { this.authPassword = authPassword; }
    public String getPrivProtocol() { return privProtocol; }
    public void setPrivProtocol(String privProtocol) { this.privProtocol = privProtocol; }
    public String getPrivPassword() { return privPassword; }
    public void setPrivPassword(String privPassword) { this.privPassword = privPassword; }
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public int getRetries() { return retries; }
    public void setRetries(int retries) { this.retries = retries; }
}
