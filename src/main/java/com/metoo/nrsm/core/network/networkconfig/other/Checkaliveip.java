package com.metoo.nrsm.core.network.networkconfig.other;

import com.metoo.nrsm.core.utils.string.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.Process;

@Slf4j
@Service
public class Checkaliveip {


    // 数据库配置（通过连接池管理）
    private final DataSource dataSource;

    // 监控配置
    private final AtomicReference<String[]> monitoredV6Ips = new AtomicReference<>();
    private final AtomicReference<String[]> monitoredV4Ips = new AtomicReference<>();
    private volatile LocalDateTime lastConfigUpdate;

    // 重试配置
    private static final int MAX_RETRIES = 3;
    private static final long CONFIG_REFRESH_MINUTES = 10;

    @Autowired
    public Checkaliveip(DataSource dataSource) {
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
//    @Scheduled(fixedRate = 60_000)
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
            String[] v6Ips = monitoredV6Ips.get();
            String[] v4Ips = monitoredV4Ips.get();
            if (v6Ips == null || v6Ips.length < 2 || v4Ips == null || v4Ips.length < 2) {
                throw new IllegalStateException("监控IP配置不完整");
            }

            // IPv6监控
            boolean v6ip1Alive = ping(v6Ips[0]);
            boolean v6ip2Alive = ping(v6Ips[1]);
            boolean v6isok = v6ip1Alive || v6ip2Alive;

            // IPv4监控
            boolean v4ip1Alive = ping(v4Ips[0]);
            boolean v4ip2Alive = ping(v4Ips[1]);
            boolean v4isok = v4ip1Alive || v4ip2Alive;

            saveMonitoringResult(conn,
                    v6ip1Alive, v6ip2Alive, v6isok,
                    v4ip1Alive, v4ip2Alive, v4isok);
            logStatus(v6ip1Alive, v6ip2Alive, v6isok,
                    v4ip1Alive, v4ip2Alive, v4isok);

        } catch (Exception e) {
            log.error("监控任务执行失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存监控结果
     */
    private void saveMonitoringResult(Connection conn,
                                      boolean v6ip1, boolean v6ip2, boolean v6isok,
                                      boolean v4ip1, boolean v4ip2, boolean v4isok) throws SQLException {

        String sql = "INSERT INTO metoo_ping(ip1status, ip2status, v6isok, " +
                "ipv41status, ipv42status, v4isok, uptime) " +
                "VALUES (?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v6ip1 ? "1" : "0");
            ps.setString(2, v6ip2 ? "1" : "0");
            ps.setString(3, v6isok ? "1" : "0");
            ps.setString(4, v4ip1 ? "1" : "0");
            ps.setString(5, v4ip2 ? "1" : "0");
            ps.setString(6, v4isok ? "1" : "0");
            ps.setString(7, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
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
                             "SELECT v6ip1, v6ip2, v4ip1, v4ip2 FROM metoo_pingipconfig LIMIT 1")) {

                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        String ipv61 = rs.getString(1);
                        String ipv62 = rs.getString(2);
                        if (StringUtils.isNotEmpty(ipv61) || StringUtils.isNotEmpty(ipv62)) {
                            List<String> ips = new ArrayList<>();
                            if (StringUtils.isNotEmpty(ipv61)) {
                                ips.add(ipv61);
                            }
                            if (StringUtils.isNotEmpty(ipv62)) {
                                ips.add(ipv62);
                            }
                            monitoredV6Ips.set(ips.toArray(new String[0]));  // 将非null的IP地址转换为数组并设置
                        }

                        String ipv41 = rs.getString(3);
                        String ipv42 = rs.getString(4);
                        if (StringUtils.isNotEmpty(ipv41) || StringUtils.isNotEmpty(ipv42)) {
                            List<String> ips = new ArrayList<>();
                            if (StringUtils.isNotEmpty(ipv41)) {
                                ips.add(ipv41);
                            }
                            if (StringUtils.isNotEmpty(ipv42)) {
                                ips.add(ipv42);
                            }
                            monitoredV4Ips.set(ips.toArray(new String[0]));  // 将非null的IP地址转换为数组并设置
                        }

                        lastConfigUpdate = LocalDateTime.now();
                        log.info("监控配置已更新: IPv6={}, IPv4={}",
                                (Object) monitoredV6Ips.get(),
                                (Object) monitoredV4Ips.get());
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
        boolean flag = lastConfigUpdate == null ||
               lastConfigUpdate.isBefore(LocalDateTime.now().minusMinutes(CONFIG_REFRESH_MINUTES));
        return flag;
    }

    public static void main(String[] args) {
        System.out.println(LocalDateTime.now().minusMinutes(10));
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
    private void logStatus(boolean v6ip1, boolean v6ip2, boolean v6isok,
                           boolean v4ip1, boolean v4ip2, boolean v4isok) {
        log.info("[状态] IPv6-1: {}, IPv6-2: {}, V6可用: {} | " +
                        "IPv4-1: {}, IPv4-2: {}, V4可用: {}",
                v6ip1 ? "1" : "0",
                v6ip2 ? "1" : "0",
                v6isok ? "1" : "0",
                v4ip1 ? "1" : "0",
                v4ip2 ? "1" : "0",
                v4isok ? "1" : "0");
    }

    @FunctionalInterface
    private interface RetryableTask<T> {
        T execute() throws Exception;
    }

}