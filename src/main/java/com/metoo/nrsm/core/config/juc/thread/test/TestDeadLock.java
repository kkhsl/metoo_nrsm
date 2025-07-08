package com.metoo.nrsm.core.config.juc.thread.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 线程死锁
 */
@Slf4j
public class TestDeadLock {

    // 测试对象锁
    public static void main(String[] args) {

        Object A = new Object();
        Object B = new Object();

        new Thread(() -> {
            synchronized (A) {
                log.info("Lock A ...");
                try {
                    Thread.sleep(1000);
                    synchronized (B) {
                        log.info("Lock B ...");
                        log.info("操作");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("执行其他代码");
            }
        }, "t1").start();

        new Thread(() -> {
            synchronized (B) {
                log.info("Lock B ...");
                try {
                    Thread.sleep(1000);
                    synchronized (A) {
                        log.info("Lock A ...");
                        log.info("操作");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("执行其他代码");
            }
        }, "t2").start();

    }

    @Test
    public void test() throws InterruptedException {

        List list = new ArrayList();// 分享变量

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    list.add(i);// 临界区
                }
            }
        });


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    list.add(i);// 临界区
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();


        log.info(String.valueOf(list.size()));
    }
}
