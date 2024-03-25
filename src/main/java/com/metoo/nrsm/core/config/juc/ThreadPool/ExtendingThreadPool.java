package com.metoo.nrsm.core.config.juc.ThreadPool;

import lombok.SneakyThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 扩展线程池
 */
public class ExtendingThreadPool {

    // 定义内部类
    private static class MyTask implements Runnable{

        private String name;

        public MyTask(String name){
            this.name = name;
        }

        @SneakyThrows
        @Override
        public void run() {
            System.out.println(name + "任务正在被线程：" + Thread.currentThread().getId() + "执行");
            Thread.sleep(1000);// 模拟执行时长
        }
    }

    public static void main(String[] args) {
        // 定义扩展线程池，可以定义线程池类继承ThreadPoolExecutor,在子类中重写beforeExecute()/afterExecute()方法
        // 也可以直接使用ThreadPoolExecutor的内部类
        ExecutorService excutorService = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>()){
            // 在内部类重写任务开始方法
            @Override
            public void beforeExecute(Thread t, Runnable r){
                System.out.println(t.getId() + "--" + "线程准备执行任务：" + ((MyTask)r).name);
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t){
                System.out.println("任务执行完毕" +  ((MyTask)r).name);
            }

            @Override
            protected void terminated(){
                System.out.println("线程退出");
            }
        };

        // 向线程池中添加任务
        for (int i = 0; i < 5; i++) {
            MyTask myTask = new MyTask("task" + i);
            excutorService.execute(myTask);
        }

        // 关闭线程池
        excutorService.shutdown();// 关闭线程池仅仅是说线程池不在接收新的任务，线程池中已接受的任务正常执行完毕
    }
}

