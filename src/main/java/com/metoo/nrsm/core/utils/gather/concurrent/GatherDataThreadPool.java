package com.metoo.nrsm.core.utils.gather.concurrent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 构建线程池
 * 参数列表（7）
 * int corePoolSize,// 核心线程数
 * int maximumPoolSize,// 最大线程数
 * long keepAliveTime,// 最大空闲时间
 * TimeUnit unit,// 时间单位
 * BlockingQueue<Runnable> workQueue,// 阻塞队列
 * ThreadFactory threadFactory// 线程工厂
 * RejectedExecutionHandler handler// 拒绝策略
 * <p>
 * 多个线程池和使用单个线程池有以下主要区别:
 * <p>
 * <p>
 * <p>
 * CPU 密集型：核心线程数=CPU核心数（CPU核心数+1）
 * 1/O 密集型:核心线程数=2*CPU核心数( CPU核心数/ (1-阻塞系数) )
 * 混合型:核心线程数=(线程等待时间/线程CPU时间+1) *CPU核心数
 * <p>
 * ==
 * 核心线程数：可以设置为与 CPU 核心数相同，或者根据任务的性质来调整。
 * 如果 Python 脚本每次执行较慢（比如 3 秒），可以适当增加核心线程数，但应避免过多。
 * <p>
 * 最大线程数：最大线程数应该根据系统的负载来设定，避免过多的线程导致系统资源被耗尽。
 * <p>
 * 队列大小：用于存储等待执行的任务。如果队列满了，线程池会根据策略拒绝任务，或者新任务会被放入一个等待队列直到线程池有空闲线程。
 * <p>
 * == 注意考虑线程执行的优先级，
 */

/**
 * 通用线程池工具类
 * 职责：仅管理线程池的生命周期和执行任务，不包含任何业务逻辑
 */
@Component
public class GatherDataThreadPool {
    private final ThreadPoolExecutor executor;
    private final String threadNamePrefix;
    private final long awaitTerminationSeconds;

    /**
     * 可配置的构造函数
     * @param threadNamePrefix 线程名前缀
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param queueCapacity 队列容量
     * @param awaitTerminationSeconds 等待终止秒数
     */
    @Autowired
    public GatherDataThreadPool(
            @Value("${thread.pool.name.prefix:gather-pool}") String threadNamePrefix,
            @Value("${thread.pool.core.size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}") int corePoolSize,
            @Value("${thread.pool.max.size:${thread.pool.core.size} * 2}") int maxPoolSize,
            @Value("${thread.pool.queue.capacity:100}") int queueCapacity,
            @Value("${thread.pool.await.termination.seconds:30}") long awaitTerminationSeconds) {

        this.threadNamePrefix = threadNamePrefix;
        this.awaitTerminationSeconds = awaitTerminationSeconds;

        this.executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new CustomThreadFactory(threadNamePrefix),
                new ThreadPoolExecutor.CallerRunsPolicy());

        // 预热核心线程
        this.executor.prestartAllCoreThreads();
    }

    /**
     * 提交任务到线程池
     * @param task 待执行的任务
     * @throws RejectedExecutionException 当任务无法被接受执行时抛出
     */
    public void execute(Runnable task) throws RejectedExecutionException {
        executor.execute(task);
    }

    /**
     * 获取线程池状态信息
     * @return 包含详细状态信息的字符串
     */
    public String getPoolStatus() {
        return String.format(
                "ThreadPool[%s] - Active: %d, PoolSize: %d, CorePoolSize: %d, " +
                        "Queue: %d/%d, Completed: %d, TaskCount: %d",
                threadNamePrefix,
                executor.getActiveCount(),
                executor.getPoolSize(),
                executor.getCorePoolSize(),
                executor.getQueue().size(),
                executor.getQueue().remainingCapacity(),
                executor.getCompletedTaskCount(),
                executor.getTaskCount());
    }

    /**
     * 获取底层ThreadPoolExecutor实例
     * 注意：谨慎使用，避免直接操作线程池
     */
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * 安全关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        if (executor.isShutdown()) {
            return;
        }

        executor.shutdown(); // 停止接受新任务
        try {
            // 等待现有任务完成
            if (!executor.awaitTermination(awaitTerminationSeconds, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // 取消正在执行的任务
                // 再次等待任务响应取消
                if (!executor.awaitTermination(awaitTerminationSeconds, TimeUnit.SECONDS)) {
                    System.err.println("线程池未能正常终止");
                }
            }
        } catch (InterruptedException ie) {
            // 重新取消当前线程的中断状态
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 自定义线程工厂
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            // 设置异常处理器
            t.setUncaughtExceptionHandler((thread, ex) -> {
                System.err.printf("线程[%s]发生未捕获异常: %s%n", thread.getName(), ex.getMessage());
                ex.printStackTrace();
            });
            return t;
        }
    }
}