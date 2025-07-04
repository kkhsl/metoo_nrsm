package com.metoo.nrsm.core.network.networkconfig.other;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.Util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableScheduling
public class Monitor {

    @Bean
    public PerformanceMonitor performanceMonitor() {
        return new PerformanceMonitor();
    }

    public static class PerformanceMonitor {

        private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        private final SystemInfo systemInfo = new SystemInfo();
        private final HardwareAbstractionLayer hardware =
                systemInfo.getHardware();

        @Autowired
        private DataSource dataSource;

        //@Scheduled(fixedRate = 60_000)
        public void monitor() {
            try (Connection conn = dataSource.getConnection()) {
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));

                // CPU计算
                CentralProcessor cpu = hardware.getProcessor();
                long[] prevTicks = cpu.getSystemCpuLoadTicks();
                Util.sleep(1000);
                double cpuUsage = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

                // 内存计算
                long totalMem = hardware.getMemory().getTotal();
                long availableMem = hardware.getMemory().getAvailable();
                double memUsage = (totalMem - availableMem) * 100.0 / totalMem;
                logger.info("[状态] - CPU: {}%, Memory: {}%", cpuUsage, memUsage);
                // 存储数据
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO metoo_performance(cpu_percent,memory_percent,uptime) VALUES(?,?,?)")) {

                    ps.setDouble(1, Math.round(cpuUsage * 10) / 10.0);
                    ps.setDouble(2, Math.round(memUsage * 10) / 10.0);
                    ps.setString(3, FORMATTER.format(now));
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            }
        }
    }
}