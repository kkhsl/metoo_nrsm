package com.metoo.nrsm.core.config.juc.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

/**
 * Semaphore: 信号量
 */
@Slf4j
public class TestSemaphore {

    public static void main(String[] args) {
        // 1, 创建semaphore 对象
        Semaphore semaphore = new Semaphore(3);

        // 2, 10个线程同时运行
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    log.info("running......");
                    Thread.sleep(1000);
                    log.info("end......");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            }).start();
        }

    }
}
