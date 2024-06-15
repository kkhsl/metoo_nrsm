package com.metoo.nrsm.core.config.juc.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池的基本使用
 */
public class MyNewFixedThreadPool {

    public static void main(String[] args) {
        // 创建有五个线程大小的线程池
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(5);
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
        ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
//        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(1, 1, new BlockingQueue<Runnable>());

        // 向线程池提交18个任务
        for (int i = 0; i < 18; i++) {
            newFixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getId() + "编号的任务在执行任务，开始时间：" +
                            System.currentTimeMillis());
                    try {
                        Thread.sleep(3000);// 模拟任务执行时长
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
