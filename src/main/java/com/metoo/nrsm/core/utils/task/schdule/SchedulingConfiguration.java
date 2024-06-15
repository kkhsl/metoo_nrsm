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
//@EnableScheduling
@Configuration
public class SchedulingConfiguration {

    /**
     *任务调度线程池配置
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(15);// 配置线程池大小，根据需要调整
//        taskScheduler.setPoolSize(36);
        taskScheduler.setThreadNamePrefix("scheduled-gather-");
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





//    @Bean
//    public ThreadPoolTaskScheduler taskScheduler(ExecutorService taskExecutor) {
//        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//        scheduler.setPoolSize(12); // 根据需要调整线程池大小
//        scheduler.setThreadNamePrefix("scheduled-task-");
//        scheduler.setWaitForTasksToCompleteOnShutdown(true);
//        scheduler.setAwaitTerminationSeconds(60); // 等待任务完成的时间
//        scheduler.setRejectedExecutionHandler((r, executor) -> {
//            // 打印被拒绝的任务信息
//            System.err.println("Task " + r.toString() + " rejected from " + executor.toString());
//        });
//        return scheduler;
//    }
//
//    @Bean
//    public ExecutorService taskExecutor() {
//        // 自定义线程池，根据需要调整线程数
//        return Executors.newFixedThreadPool(10);
//    }

}
