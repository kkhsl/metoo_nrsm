package com.metoo.nrsm.core.utils.task.schdule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 配置自定义线程池和调度器
 */
@Configuration
public class SchedulingConfiguration {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(15);// 配置线程池大小，根据需要调整
        taskScheduler.initialize();
        taskScheduler.setThreadNamePrefix("scheduled-gather-");
        return taskScheduler;
    }
}
