package com.metoo.nrsm.core.config.juc.thread.method.priority;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestThread {

    Logger log = LoggerFactory.getLogger(TestThread.class);

    public static void main(String[] args) {
        for (int i = 1; i <= 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int sum = 0;
                    for (int i = 0; i <= 10; i++) {
                        sum += i;
                        System.out.println(Thread.currentThread().getName() + " num: " + i);
                    }
                }
            }).start();
        }
    }

    // 测试主线程等待子线程执行
    @org.junit.Test
    public void testJoin() throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("子线程休眠: 5秒");
                try {
                    Thread.sleep(5000);
                    log.info("子线程：" + Thread.currentThread().getName() + " 休眠结束");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
        Thread.currentThread().join(6000);
        log.info("主线程：" + Thread.currentThread().getName() + " 等待");
        log.info("主线程执行结束");
    }

    /**
     * 主线程成等待线程池内子线程完成在执行
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test() throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(3);

        Future<Integer> task1 = pool.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        });

        Future<Integer> task2 = pool.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 3;
        });

        //不再接受新的任务
        pool.shutdown();

        //get方法为阻塞获取
        System.out.println("task1的运行结果:" + task1.get());
        System.out.println("task2的运行结果:" + task2.get());

        System.out.println("主线程结束");
    }

    // 测试自定义线程池结束是否会影响其他线程池

}
