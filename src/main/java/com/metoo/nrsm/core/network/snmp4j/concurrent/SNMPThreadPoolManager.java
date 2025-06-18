package com.metoo.nrsm.core.network.snmp4j.concurrent;

import java.util.List;
import java.util.concurrent.*;

public class SNMPThreadPoolManager {

    // 创建一个单例线程池，避免多次创建
//    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);  // 假设最大线程数为 10

    private static final int CORE_POOL_SIZE = 10;    // 核心线程数
    private static final int MAX_POOL_SIZE = 100;    // 最大线程数
    private static final long KEEP_ALIVE_TIME = 60L; // 线程空闲的最大存活时间（单位：秒）

    private static final ExecutorService executorService = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(100),
            new ThreadPoolExecutor.CallerRunsPolicy()  // 当队列满时，主线程执行任务
    );


    // 获取线程池实例
    public static ExecutorService getExecutorService() {
        return executorService;
    }

    // 关闭线程池
    public static void shutdown() {
        executorService.shutdown();
    }

    // 提交并发任务
    public static <T> List<Future<T>> submitTasks(List<Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(tasks);
    }

}
