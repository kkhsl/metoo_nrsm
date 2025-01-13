package com.metoo.nrsm.core.utils.gather.thread;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * 构建线程池
 */

@Component
public class GatherDataThreadPool {

    private final ExecutorService fixedThreadPool;


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
