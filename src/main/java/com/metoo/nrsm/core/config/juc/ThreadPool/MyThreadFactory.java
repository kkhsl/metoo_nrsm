package com.metoo.nrsm.core.config.juc.ThreadPool;

import java.util.Random;
import java.util.concurrent.*;

/**
 * 自定义线程工厂
 */
public class MyThreadFactory {

    public static void main(String[] args) {
        Runnable runnerble = new Runnable() {
            @Override
            public void run() {
                int num = new Random().nextInt();
                System.out.println(num);
                System.out.println(Thread.currentThread().getId() + "--" + System.currentTimeMillis() + "开始睡眠：" + num + "秒");
            }
        };

        // 创建线程池，使用自定义线程工厂
        ExecutorService executorsService = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                // 根据参数r接受的任务，创建一个线程
                Thread t = new Thread(r);
                t.setDaemon(true);// 设置为守护线程，当主线程运行结束，线程池中的线程会自动退出，释放
                System.out.println("创建了线程：" + t);
                return t;
            }
        });

        for (int i = 0; i < 5; i++) {
            executorsService.execute(runnerble);
        }

        // 主线程睡眠
        try {
            Thread.sleep(10000);
            // 主线程睡眠超时，主线程结束，线程池中的线程会自动退出
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
