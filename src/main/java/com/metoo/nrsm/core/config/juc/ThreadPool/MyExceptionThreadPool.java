package com.metoo.nrsm.core.config.juc.ThreadPool;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * 线程池-异常未显示
 */
public class MyExceptionThreadPool {

    /**
     * 自定义类实现Runnable接口，用于计算两个数相除
     */
    private static class DivideTask implements Runnable {

        private int x;
        private int y;

        public DivideTask(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getId() + "计算：" + x + "/" + y + (x / y));
        }
    }

    public static void main(String[] args) {
        // 创建线程池
//        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
        // 创建自定义线程池
        ExtendingThreadPoolExecutor poolExecutor = new ExtendingThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
        // 向线程池中添加计算两个数相除的任务
        for (int i = 0; i < 5; i++) {
            poolExecutor.submit(new DivideTask(10, i));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            poolExecutor.execute(new DivideTask(10, i));
        }// 执行结果只有四条，实际线程数为5条，10/0未显示；当i等于0时，线程池将算数异常吃掉，导致无法感知异常
        /**
         * 解决方法
         *  一：将submit提交方法改为execute();
         *  二：对线程池进行扩展，对submit()方法进行包装；
         */


    }
}
