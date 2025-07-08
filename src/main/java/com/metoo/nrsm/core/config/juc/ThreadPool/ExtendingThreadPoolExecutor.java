package com.metoo.nrsm.core.config.juc.ThreadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程池类，对ThreadPoolExecutor进行扩展
 */
public class ExtendingThreadPoolExecutor extends ThreadPoolExecutor {

    // 自定义线程池类
    public ExtendingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    // 定义方法，对执行的任务进行包装，接收两个参数，第一个参数接收要执行的任务，第二个参数是一个Exception异常
    public Runnable wrap(Runnable task, Exception exception) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    exception.printStackTrace();
                    throw e;
                }
            }
        };
    }

    // 重写submit方法
    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(wrap(task, new Exception("客户跟踪异常")));
    }

    // 重写execute方法
    @Override
    public void execute(Runnable task) {
        super.execute(wrap(task, new Exception("客户跟踪异常")));
    }
}
