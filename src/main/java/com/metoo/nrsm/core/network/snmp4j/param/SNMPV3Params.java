package com.metoo.nrsm.core.network.snmp4j.param;

import org.snmp4j.security.SecurityLevel;

public class SNMPV3Params {
    private final String version; // "v1", "v2c", "v3"
    private final String host;
    private final int port;
    private final int securityLevel;
    private final String community; // v1/v2c使用
    private final String username;  // v3使用
    private final String authProtocol; // v3使用
    private final String authPassword; // v3使用
    private final String privProtocol; // v3使用(仅authPriv级别)
    private final String privPassword; // v3使用(仅authPriv级别)
    private final int timeout;
    private final int retries;

    // 私有构造方法，通过Builder创建
    private SNMPV3Params(Builder builder) {
        this.version = builder.version;
        this.host = builder.host;
        this.port = builder.port;
        this.securityLevel = builder.securityLevel;
        this.community = builder.community;
        this.username = builder.username;
        this.authProtocol = builder.authProtocol;
        this.authPassword = builder.authPassword;
        this.privProtocol = builder.privProtocol;
        this.privPassword = builder.privPassword;
        this.timeout = builder.timeout;
        this.retries = builder.retries;
    }

    public static class Builder {
        // 必需参数
        private String version;
        private String host;

        // 可选参数 - 带默认值
        private int port = 161;
        private int securityLevel;
        private String community;
        private String username;
        private String authProtocol;
        private String authPassword;
        private String privProtocol;
        private String privPassword;
        private int timeout = 1000; // 默认1秒
        private int retries = 2;   // 默认重试2次

        public Builder version(String version) {
            if (!"v1".equalsIgnoreCase(version) &&
                    !"v2c".equalsIgnoreCase(version) &&
                    !"v3".equalsIgnoreCase(version)) {
                throw new IllegalArgumentException("Invalid SNMP version. Must be v1, v2c or v3");
            }
            this.version = version.toLowerCase();
            return this;
        }

        public Builder host(String host) {
            if (host == null || host.trim().isEmpty()) {
                throw new IllegalArgumentException("Host cannot be null or empty");
            }
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535");
            }
            this.port = port;
            return this;
        }

        public Builder securityLevel(int securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }

        public Builder community(String community) {
            this.community = community;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder authProtocol(String authProtocol) {
            this.authProtocol = authProtocol;
            return this;
        }

        public Builder authPassword(String authPassword) {
            this.authPassword = authPassword;
            return this;
        }

        public Builder privProtocol(String privProtocol) {
            this.privProtocol = privProtocol;
            return this;
        }

        public Builder privPassword(String privPassword) {
            this.privPassword = privPassword;
            return this;
        }

        public Builder timeout(int timeout) {
            if (timeout <= 0) {
                throw new IllegalArgumentException("Timeout must be positive");
            }
            this.timeout = timeout;
            return this;
        }

        public Builder retries(int retries) {
            if (retries < 0) {
                throw new IllegalArgumentException("Retries cannot be negative");
            }
            this.retries = retries;
            return this;
        }

        public SNMPV3Params build() {
            // 验证必需参数
            if (version == null) {
                throw new IllegalStateException("SNMP version must be specified");
            }
            if (host == null) {
                throw new IllegalStateException("Host must be specified");
            }

            // 版本特定验证
            switch (version) {
                case "v1":
                case "v2c":
                    if (community == null || community.trim().isEmpty()) {
                        throw new IllegalStateException(version.toUpperCase() + " requires community string");
                    }
                    break;

                case "v3":
                    if (username == null || username.trim().isEmpty()) {
                        throw new IllegalStateException("SNMPv3 requires username");
                    }
                    // 注意：v3的安全级别验证由工厂类处理
                    break;
            }

            return new SNMPV3Params(this);
        }
    }


    // Getter 方法
    public String getVersion() {
        return version;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }


    public int getSecurityLevel() {
        return securityLevel;
    }


    public String getCommunity() {
        return community;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthProtocol() {
        return authProtocol;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public String getPrivProtocol() {
        return privProtocol;
    }

    public String getPrivPassword() {
        return privPassword;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getRetries() {
        return retries;
    }

    @Override
    public String toString() {
        return "SNMPParam{" +
                "version='" + version + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", community='" + (community != null ? "[PROTECTED]" : "null") + '\'' +
                ", username='" + username + '\'' +
                ", authProtocol='" + authProtocol + '\'' +
                ", authPassword='" + (authPassword != null ? "[PROTECTED]" : "null") + '\'' +
                ", privProtocol='" + privProtocol + '\'' +
                ", privPassword='" + (privPassword != null ? "[PROTECTED]" : "null") + '\'' +
                ", timeout=" + timeout +
                ", retries=" + retries +
                '}';
    }

}
