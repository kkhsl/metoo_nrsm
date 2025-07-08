package com.metoo.nrsm.core.config.juc.ThreadPool;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义拒绝策略
 */
public class MyRejectedExecutionHandler {

    // 自定义任务
    public static void main(String[] args) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                int num = new Random().nextInt(5);
                System.out.println(Thread.currentThread().getId() + "---" + System.currentTimeMillis() + " 开始睡眠" + num + "秒");
                try {
                    TimeUnit.SECONDS.sleep(num);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
//        // 创建线程池
//        ThreadPoolExecutor threadPoolExecutor =
//                new ThreadPoolExecutor(
//                    5,
//                    5,
//                    0,
//                    TimeUnit.SECONDS,
//                    new LinkedBlockingQueue<>(10),
//                    Executors.defaultThreadFactory());
        // 创建线程池,自定义拒绝策略
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(
                        5,
                        5,
                        0,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(10),
                        new RejectedExecutionHandler() {
                            @Override
                            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                // r就是请求的任务；executor就是当前线程池
                                System.out.println(r + " is discarding");
                            }
                        });

        // 向线程池提交若干任务
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            threadPoolExecutor.submit(run);
        }
    }


}
