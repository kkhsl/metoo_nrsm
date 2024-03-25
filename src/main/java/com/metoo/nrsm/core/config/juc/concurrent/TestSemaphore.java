package com.metoo.nrsm.core.config.juc.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-25 11:38
 *
 * Semaphore: 信号量
 * 应用:
 *      使用semaphore限流,在访问高峰期时,让请求线程阻塞,高峰期过去在释放许可,当然它只适合限制单机线程数量,并且仅是限制线程数,而不是限制资源数(例如连接数,请对比Tomcat LimitLatch的实现)
 *      使用Semaphore实现简单连接池,对比[享元模式]下的实现(用wait notify),性能和可读性显然更好,注意下面的实现中线程数和数据库连接数是相等的
 *
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
