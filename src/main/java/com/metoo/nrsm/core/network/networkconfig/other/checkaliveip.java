package com.metoo.nrsm.core.network.networkconfig.other;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class checkaliveip {

    // 数据库配置（通过连接池管理）
    private final DataSource dataSource;
    
    // 监控配置
    private final AtomicReference<String[]> monitoredIps = new AtomicReference<>();
    private volatile LocalDateTime lastConfigUpdate;

    // 重试配置
    private static final int MAX_RETRIES = 3;
    private static final long CONFIG_REFRESH_MINUTES = 10;

    @Autowired
    public checkaliveip(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 初始化加载配置
     */
    @PostConstruct
    private void init() {
        refreshMonitoringConfig(true);
    }

    /**
     * 定时监控任务（每分钟执行）
     */
    //@Scheduled(fixedRate = 60_000)
    public void scheduledMonitoring() {
        // 配置自动刷新
        if (shouldRefreshConfig()) {
            refreshMonitoringConfig(false);
        }

        // 执行监控逻辑
        executeWithRetry(() -> {
            try (Connection conn = dataSource.getConnection()) {
                performMonitoring(conn);
                return null;
            }
        }, MAX_RETRIES);
    }

    /**
     * 执行监控核心逻辑
     */
    private void performMonitoring(Connection conn) {
        try {
            String[] ips = monitoredIps.get();
            if (ips == null || ips.length < 2) {
                throw new IllegalStateException("监控IP未正确配置");
            }

            boolean ip1Alive = ping(ips[0]);
            boolean ip2Alive = ping(ips[1]);
            boolean networkHealthy = ip1Alive || ip2Alive;

            saveMonitoringResult(conn, ip1Alive, ip2Alive, networkHealthy);
            logStatus(ip1Alive, ip2Alive, networkHealthy);

        } catch (Exception e) {
            log.error("监控任务执行失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存监控结果
     */
    private void saveMonitoringResult(Connection conn, boolean ip1, boolean ip2, boolean network) 
        throws SQLException {
        
        String sql = "INSERT INTO metoo_ping(ip1status, ip2status, v6isok, uptime) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip1 ? "1" : "0");
            ps.setString(2, ip2 ? "1" : "0");
            ps.setString(3, network ? "1" : "0");
            ps.setString(4, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ps.executeUpdate();
        }
    }

    /**
     * 刷新监控配置
     */
    private synchronized void refreshMonitoringConfig(boolean force) {
        if (force || shouldRefreshConfig()) {
            executeWithRetry(() -> {
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT v6ip1, v6ip2 FROM metoo_pingipconfig LIMIT 1")) {
                    
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        monitoredIps.set(new String[]{rs.getString(1), rs.getString(2)});
                        lastConfigUpdate = LocalDateTime.now();
                        log.info("监控配置已更新: {}", (Object) monitoredIps.get());
                    }
                    return null;
                }
            }, MAX_RETRIES);
        }
    }

    /**
     * 带重试机制的通用执行方法
     */
    private <T> T executeWithRetry(RetryableTask<T> task, int retries) {
        try {
            return task.execute();
        } catch (Exception e) {
            if (retries > 0) {
                log.warn("操作失败，剩余重试次数: {}，原因: {}", retries, e.getMessage());
                return executeWithRetry(task, retries - 1);
            }
            throw new RuntimeException("操作重试次数耗尽", e);
        }
    }

    /**
     * 判断是否需要刷新配置
     */
    private boolean shouldRefreshConfig() {
        return lastConfigUpdate == null || 
               lastConfigUpdate.isBefore(LocalDateTime.now().minusMinutes(CONFIG_REFRESH_MINUTES));
    }

    /**
     * Ping操作实现
     */
    private boolean ping(String ip) throws IOException, InterruptedException {
        String[] command = System.getProperty("os.name").toLowerCase().contains("win") ?
            new String[]{"ping", "-n", "1", ip} :
            new String[]{"ping", "-c", "1", ip};

        Process process = new ProcessBuilder(command).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines().anyMatch(line -> line.toLowerCase().contains("ttl=")) && 
                   process.waitFor() == 0;
        }
    }

    /**
     * 状态日志记录
     */
    private void logStatus(boolean ip1status, boolean ip2status, boolean v6isok) {
        log.info("[状态] IP1: {}, IP2: {}, 可用: {}",
                ip1status ? "1" : "0",
                ip2status ? "1" : "0",
                v6isok ? "1" : "0");
    }

    @FunctionalInterface
    private interface RetryableTask<T> {
        T execute() throws Exception;
    }
}