package com.metoo.nrsm.core.config.juc.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-25 16:34
 *
 * CyclicBarrirer：循环栅栏
 * 应用场景：用于多线程计算数据，最后合并计算结果的场景
 * 注意事项：线程数和任务数量保持一致
 *
 */
@Slf4j
public class TestCyclicBarrirer {

    public static void main(String[] args) {

        ExecutorService service = Executors.newFixedThreadPool(2);

        CyclicBarrier cyclicBarrier = new CyclicBarrier(2, () ->{
            log.debug("task end.......");
        });


        service.submit(() -> {
            log.debug("task1 runing.......");
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        });

        service.submit(() -> {
            log.debug("task2 runing.......");
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        });


    }
}
