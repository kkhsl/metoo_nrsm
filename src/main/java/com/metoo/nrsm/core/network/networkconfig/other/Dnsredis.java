package com.metoo.nrsm.core.network.networkconfig.other;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dnsredis {
    private static final Logger log = LoggerFactory.getLogger(Dnsredis.class);
    private static final int CACHE_TTL_SECONDS = 300;
    private static final int MAX_CACHE_RECORDS = 1000;
    private static final int DNS_PACKET_SIZE = 512;

    // 配置参数（从配置文件加载）
    private static int dnsPort;
    private static String redisHost;
    private static String redisPassword;
    private static String mysqlUrl;
    private static String mysqlUser;
    private static String mysqlPassword;
    private static String upstreamDns;

    // 连接池实例
    private static JedisPool jedisPool;
    private static HikariDataSource dataSource;

    public static void main(String[] args) {
        try {
            initialize();
            startServer();
        } catch (Exception e) {
            log.error("DNS server startup failed", e);
            System.exit(1);
        }
    }

    private static void initialize() throws Exception {
        loadConfig();
        setupConnectionPools();
        validateDependencies();
    }

    private static void loadConfig() {
        try {
            Properties prop = new Properties();
            prop.load(Dnsredis.class.getResourceAsStream("/application-test.properties"));
            
            dnsPort = Integer.parseInt(prop.getProperty("dns.port", "53"));
            redisHost = prop.getProperty("spring.redis.redis.host");
            redisPassword = prop.getProperty("spring.redis.redis.password");
            mysqlUrl = prop.getProperty("spring.datasource.url");
            mysqlUser = prop.getProperty("spring.datasource.user");
            mysqlPassword = prop.getProperty("spring.datasource.password");
            upstreamDns = prop.getProperty("upstream.dns");
        } catch (Exception e) {
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    private static void setupConnectionPools() {
        // Redis连接池配置
        JedisPoolConfig redisConfig = new JedisPoolConfig();
        redisConfig.setMaxTotal(200);
        redisConfig.setMaxIdle(50);
        redisConfig.setMinIdle(10);
        redisConfig.setMaxWaitMillis(3);
        jedisPool = new JedisPool(redisConfig, redisHost, 6379, 2000, redisPassword);

        // MySQL连接池配置
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mysqlUrl);
        hikariConfig.setUsername(mysqlUser);
        hikariConfig.setPassword(mysqlPassword);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtsCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtsCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(hikariConfig);
    }

    private static void validateDependencies() throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
        }
        try (Connection conn = dataSource.getConnection()) {
            conn.isValid(3);
        }
        InetAddress.getByName(upstreamDns);
    }

    private static void startServer() throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try (DatagramSocket socket = new DatagramSocket(dnsPort)) {
            log.info("DNS server started on port {}", dnsPort);
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[DNS_PACKET_SIZE], DNS_PACKET_SIZE);
                socket.receive(packet);
                executor.execute(new DnsRequestHandler(packet, socket));
            }
        }
    }

    static class DnsRequestHandler implements Runnable {
        private final DatagramPacket request;
        private final DatagramSocket socket;

        DnsRequestHandler(DatagramPacket request, DatagramSocket socket) {
            this.request = request;
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Jedis redis = jedisPool.getResource();
                 Connection mysqlConn = dataSource.getConnection()) {
                
                Message query = new Message(request.getData());
                Record question = query.getQuestion();
                String qname = question.getName().toString(true);
                int qtype = question.getType();

                Message response = processQuery(qname, qtype, redis, mysqlConn);
                sendResponse(response);
            } catch (Exception e) {
                log.error("Query processing failed: {}", e.getMessage());
            }
        }

        private Message processQuery(String qname, int qtype, Jedis redis, Connection mysqlConn) {
            try {
                // 1. 检查缓存
                List<String> cached = redis.lrange(qname, 0, -1);
                if (!cached.isEmpty()) {
                    return buildCachedResponse(qname, qtype, cached);
                }

                // 2. 检查数据库
                if (checkDatabase(qname, mysqlConn, redis)) {
                    return processQuery(qname, qtype, redis, mysqlConn); // 递归重试
                }

                // 3. 上游查询
                Message upstreamResponse = queryUpstream(qname, qtype);
                cacheResponse(qname, upstreamResponse, redis);
                return upstreamResponse;
            } catch (Exception e) {
                return buildErrorResponse();
            }
        }

        private Message buildCachedResponse(String qname, int qtype, List<String> records) {
            Message response = new Message();
            response.getHeader().setFlag(Flags.QR);
            
            records.stream()
                .map(record -> parseDnsRecord(qname, record))
                .filter(r -> r != null && r.getType() == qtype)
                .forEach(r -> response.addRecord(r, Section.ANSWER));

            return response;
        }

        private Record parseDnsRecord(String qname, String record) {
            try {
                String[] parts = record.split(":", 2);
                int type = Type.value(parts[0]);
                return Record.fromString(
                    Name.fromString(qname), 
                    type, 
                    DClass.IN, 
                    CACHE_TTL_SECONDS, 
                    parts[1], 
                    Name.root
                );
            } catch (Exception e) {
                log.warn("Invalid cache record: {}", record);
                return null;
            }
        }

        private boolean checkDatabase(String qname, Connection conn, Jedis redis) {
            String sql = "SELECT rtype, address FROM metoo_dns WHERE qname = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, qname);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String type = rs.getString("rtype");
                    String addr = rs.getString("address");
                    redis.rpush(qname, type + ":" + addr);
                }
                redis.expire(qname, CACHE_TTL_SECONDS);
                return rs.getRow() > 0;
            } catch (SQLException e) {
                log.error("Database query failed", e);
                return false;
            }
        }

        private Message queryUpstream(String qname, int qtype) throws IOException {
            Lookup lookup = new Lookup(qname, qtype);
            lookup.setResolver(new ExtendedResolver(new String[]{upstreamDns}));
            lookup.run();

            Message response = new Message();
            response.getHeader().setFlag(Flags.QR);

            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                for (Record record : lookup.getAnswers()) {
                    response.addRecord(record, Section.ANSWER);
                }
            } else {
                response.getHeader().setRcode(Rcode.SERVFAIL);
            }
            return response;
        }

        private void cacheResponse(String qname, Message response, Jedis redis) {
            for (Record record : response.getSectionArray(Section.ANSWER)) {
                redis.rpush(qname, Type.string(record.getType()) + ":" + record.rdataToString());
            }
            redis.expire(qname, CACHE_TTL_SECONDS);
            
            // 缓存淘汰策略
            if (redis.llen(qname) > MAX_CACHE_RECORDS) {
                redis.ltrim(qname, 0, MAX_CACHE_RECORDS - 1);
            }
        }

        private void sendResponse(Message response) throws IOException {
            byte[] responseData = response.toWire();
            DatagramPacket responsePacket = new DatagramPacket(
                responseData, 
                responseData.length,
                request.getAddress(),
                request.getPort()
            );
            socket.send(responsePacket);
        }

        private Message buildErrorResponse() {
            Message response = new Message();
            response.getHeader().setRcode(Rcode.SERVFAIL);
            return response;
        }
    }
}