package com.metoo.nrsm.core.config.juc.future;

import com.metoo.nrsm.core.utils.gather.thread.GatherDataThreadPool;

import java.util.concurrent.CountDownLatch;

/**
 *
 // 某个线程开始执行前等待其他线程执行完(阻塞)
 */
public class CountDownLatchDemo2 {

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++){
            try {
                GatherDataThreadPool.getInstance().addThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                            System.out.println("running2......" + Thread.currentThread().getName());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }finally{
                            latch.countDown();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {

            System.out.println(latch.getCount());

            latch.await();// 等待结果线程池线程执行结束

            System.out.println(latch.getCount());

            System.out.println("run2 end......");

            //等待5秒
//                TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
