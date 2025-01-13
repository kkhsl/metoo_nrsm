package com.metoo.nrsm.core.config.juc.future;

import com.metoo.nrsm.core.utils.gather.thread.GatherDataThreadPool;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchDemo {

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(11);

        for (int i = 0; i < 10; i++){
            try {
                GatherDataThreadPool.getInstance().addThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                            System.out.println("running......" + Thread.currentThread().getName());
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

            System.out.println("run end......");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
