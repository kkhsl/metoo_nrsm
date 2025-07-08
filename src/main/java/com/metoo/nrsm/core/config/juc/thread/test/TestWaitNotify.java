package com.metoo.nrsm.core.config.juc.thread.test;

import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.sleep;

@Slf4j
public class TestWaitNotify {

    private static Object obj = new Object();

    // 测试对象锁
    public static void main(String[] args) {

        new Thread(() -> {
            synchronized (obj) {// 获取obj锁对象，然后才可以执行wait()
                log.info("执行...");
                try {
                    obj.wait();// 让线程在obj上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("执行其他代码");
            }
        }, "t1").start();

        new Thread(() -> {
            synchronized (obj) {
                log.info("执行...");
                try {
                    obj.wait();// 让线程在obj上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("执行其他代码");
            }
        }, "t2").start();

        // 主线程两秒后执行
        try {
            Thread.sleep(500);
            log.info("唤醒obj上的其他线程");
            synchronized (obj) {
                obj.notify(); // 唤醒obj上的一个线程
//                 obj.notifyAll(); // 唤醒obj上的所有线程
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
