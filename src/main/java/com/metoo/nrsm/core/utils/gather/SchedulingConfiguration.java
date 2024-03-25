package com.metoo.nrsm.core.utils.gather;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

//@EnableScheduling
@Configuration
public class SchedulingConfiguration {

    /**
     *任务调度线程池配置
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(11);
        return taskScheduler;
    }

//    @Bean(destroyMethod="shutdown")
//    public Executor taskExecutor() {
//        return Executors.newScheduledThreadPool(10); //指定Scheduled线程池大小
//    }

    /**
     * 异步任务执行线程池
     * @return
     */
//    @Bean(name = "asyncExecutor")
//    public ThreadPoolTaskExecutor asyncExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(10);
//        executor.setQueueCapacity(1000);
//        executor.setKeepAliveSeconds(600);
//        executor.setMaxPoolSize(20);
//        executor.setThreadNamePrefix("taskExecutor-");
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        executor.initialize();
//        return executor;
//    }

}
