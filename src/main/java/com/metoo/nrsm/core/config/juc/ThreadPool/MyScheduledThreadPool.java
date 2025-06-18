package com.metoo.nrsm.core.config.juc.ThreadPool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 线程池计划任务
 */
public class MyScheduledThreadPool {

//    public static void main(String[] args) {
//        // 创建一个有调度功能的线程池
//        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
//        // 在延迟两秒后执行任务
//        // schedule(Runnerble任务，延迟时长，时间单位)
//        scheduledExecutorService.schedule(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println(Thread.currentThread().getId() + "编号的任务在执行任务，开始时间：" +
//                        System.currentTimeMillis());
//            }
//        },2,TimeUnit.SECONDS);
//
//        // 以固定频率执行任务，开启任务的时间是固定的，在3秒后执行任务，以后没隔5秒重新执行一次
//        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println(Thread.currentThread().getId() + "编号的任务在执行任务，开始时间：" +
//                        System.currentTimeMillis() + "以固定频率开启任务");
//                // 睡眠6秒
//                try {
//                    TimeUnit.SECONDS.sleep(6000);// 如果任务执行时间超过任务间隔时间，任务完成后立即开启下一个任务
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        },3, 5, TimeUnit.SECONDS);
//
//        // 在上次任务结束后，在固定延迟后再次执行该任务
//        // 不管执行任务耗时多久，总是在任务结束后的2秒再次开启新的任务
//        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println(Thread.currentThread().getId() + "编号的任务在执行任务，开始时间：" +
//                        System.currentTimeMillis() + "以固定频率开启任务");
//                // 睡眠6秒
//                try {
//                    TimeUnit.SECONDS.sleep(3);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 3, 2, TimeUnit.SECONDS);
//    }
}
