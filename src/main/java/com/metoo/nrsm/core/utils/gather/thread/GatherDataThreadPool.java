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
 * ==
 * 核心线程数：可以设置为与 CPU 核心数相同，或者根据任务的性质来调整。
 * 如果 Python 脚本每次执行较慢（比如 3 秒），可以适当增加核心线程数，但应避免过多。
 *
 * 最大线程数：最大线程数应该根据系统的负载来设定，避免过多的线程导致系统资源被耗尽。
 *
 * 队列大小：用于存储等待执行的任务。如果队列满了，线程池会根据策略拒绝任务，或者新任务会被放入一个等待队列直到线程池有空闲线程。
 *
 * == 注意考虑线程执行的优先级，
 *
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
                poolSize, // 核心线程数为 CPU 核心数
                poolSize * 2, // 最大线程数为 CPU 核心数的 2 倍
                60, // 空闲线程存活时间为 60 秒
                TimeUnit.SECONDS, // 存活时间单位
                new LinkedBlockingQueue<>(), // 等待队列大小为 ? 设备数量
                new ThreadPoolExecutor.CallerRunsPolicy());// 如果队列满了，当前任务会在主线程中执行

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
