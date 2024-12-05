package com.metoo.nrsm.core.config.juc.ThreadPool;


import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 监控线程池
 */
public class MonitorThreadPool {

    public static void main(String[] args) {
        Runnable runnerble = new Runnable() {
            @Override
            public void run() {
                int num = new Random().nextInt();
                System.out.println(Thread.currentThread().getId() + "--" + System.currentTimeMillis() + "开始睡眠：" + num + "秒");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 定义线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 5,0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());

        // 向线程池提交30个任务
        for (int i = 0; i < 30; i++) {
            executor.submit(runnerble);
            System.out.println(
                    " 当前线程池核心线程数量：" + executor.getCorePoolSize()
                +   " 最大线程数：" + executor.getMaximumPoolSize()
                +   " 当前线程池大小：" + executor.getPoolSize()
                +   " 活动线程："       + executor.getActiveCount()
                +   " 收到任务数："    + executor.getTaskCount()
                +   " 完成任务数："    + executor.getCompletedTaskCount()
                +   " 等待任务数："    + executor.getQueue().size()
            );
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (executor.getActiveCount() >= 0){
            System.out.println(
                    " 当前线程池核心线程数量：" + executor.getCorePoolSize()
                            +   " 最大线程数：" + executor.getMaximumPoolSize()
                            +   " 当前线程池大小：" + executor.getPoolSize()
                            +   " 活动线程："       + executor.getActiveCount()
                            +   " 收到任务数："    + executor.getTaskCount()
                            +   " 完成任务数："    + executor.getCompletedTaskCount()
                            +   " 等待任务数："    + executor.getQueue().size()
            );
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
