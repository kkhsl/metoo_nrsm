package com.metoo.nrsm.core.utils.gather.thread;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * 构建线程池
 * 参数列表（7）
         int corePoolSize,// 核心线程数
         int maximumPoolSize,// 最大线程数
         long keepAliveTime,// 最大空闲时间
         TimeUnit unit,// 时间单位
         BlockingQueue<Runnable> workQueue,// 阻塞队列
         ThreadFactory threadFactory// 线程工厂
         RejectedExecutionHandler handler// 拒绝策略

 多个线程池和使用单个线程池有以下主要区别:


 * CPU 密集型：核心线程数=CPU核心数（CPU核心数+1）
 * 1/O 密集型:核心线程数=2*CPU核心数( CPU核心数/ (1-阻塞系数) )
 * 混合型:核心线程数=(线程等待时间/线程CPU时间+1) *CPU核心数
 *

 */

@Component
public class GatherDataThreadPool {

    private final ExecutorService fixedThreadPool;

    // 线程池大小通过构造函数动态传入
//    @Autowired
//    public GatherDataThreadPool() {
//        int poolSize = Integer.max(Runtime.getRuntime().availableProcessors() * 3, 15);
//        this.fixedThreadPool = Executors.newFixedThreadPool(poolSize);
//    }

    @Autowired
    public GatherDataThreadPool() {
        int poolSize = Integer.max(Runtime.getRuntime().availableProcessors() * 3, 15);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                poolSize, // 核心池大小
                poolSize * 2, // 最大池大小
                60, // 空闲线程的最大存活时间
                TimeUnit.SECONDS, // 存活时间单位
                new LinkedBlockingQueue<>(), // 任务队列
                new ThreadPoolExecutor.CallerRunsPolicy()); // 如果队列满了，当前线程执行任务

        this.fixedThreadPool = executor;
    }

    /**
     * 获取线程池单例
     *
     * @return 线程池实例
     */
    public ExecutorService getService() {
        return fixedThreadPool;
    }


    /**
     * 向线程池提交任务
     *
     * @param run 任务
     */
    public void addThread(Runnable run) {
        fixedThreadPool.execute(run);
    }

    /**
     * 关闭线程池
     */
//    public void shutdown() {
//        fixedThreadPool.shutdown();
//    }

    @PreDestroy
    public void shutdown() {
        try {
            if (!fixedThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                fixedThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            fixedThreadPool.shutdownNow();
        }
    }

    private static GatherDataThreadPool pool = new GatherDataThreadPool();// 创建单例

    public static GatherDataThreadPool getInstance(){
        return pool;
    }

}
