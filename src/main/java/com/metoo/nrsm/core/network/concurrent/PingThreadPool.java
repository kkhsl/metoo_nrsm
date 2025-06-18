package com.metoo.nrsm.core.network.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 虽然是轻量级操作，但数量大时仍需要并发执行以减少总耗时。
 *
 * 为什么重新开一个线程池：如果 SNMP数据采集任务耗时远高于 Ping，可能导致 Ping 任务被阻塞。
 *
 * CPU核心数	CPU核心数 * 2、无界队列（或大容量）、轻量级任务、快速执行
 */
public class PingThreadPool {

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final int MAX_THREADS = Math.min(32, CPU_CORES * 2); // 限制最大线程数
    private static final ThreadPoolExecutor executor;

    // 全局单例
    static {
        executor = new ThreadPoolExecutor(
                MAX_THREADS,
                MAX_THREADS,
                30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), // 有界队列防OOM
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时由调用线程执行
        );

        // 注册关闭钩子（静态块中，确保线程池创建后立即绑定钩子）
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
    }

    public static void execute(Runnable task) {
        executor.execute(task);
    }

    public static void shutdown() {
        executor.shutdown();
    }

    public static void shutdownNow() {
        executor.shutdownNow();
    }

    // 新增方法：等待线程池终止
    public static boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    // 监控线程池状态（可选）
    public static void printStats() {
        System.out.printf(
                "Pool Stats: Active=%d, Queue=%d, Completed=%d%n",
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount()
        );
    }
}
