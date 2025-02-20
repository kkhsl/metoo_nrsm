package com.metoo.nrsm.core.config.juc.thread.method.create;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {

    Logger log = LoggerFactory.getLogger(ThreadTest.class);

    public static void main(String[] args) {

        System.out.println("JVM启动main线程，main线程执行main方法：打印当前线程名称" + Thread.currentThread().getName());
        // 创建子线程
        MyThread myThread = new MyThread();
        MyThread myThread1 = new MyThread();
        myThread1.run();
        System.out.println("线程后的其他方法");
    }

    ExecutorService exe = Executors.newFixedThreadPool(5);
    /**
     * 测试主线程等待子线程执行结束
     */
    @Test
    public void test(){

        for (int i = 1; i <= 10; i ++){
            int finalI = i;
            exe.execute(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            System.out.println(Thread.currentThread().getName() + " number " + finalI);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                })
            );
        }
        if(exe != null){
            exe.shutdown();
        }
        while (true) {
            if (exe.isTerminated()) {
                break;
            }
        }
        System.out.println("running end......");

        for (int i = 1; i <= 10; i ++){
            int finalI = i;
            exe.execute(
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(5000);
                                System.out.println(Thread.currentThread().getName() + " number " + finalI);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    })
            );
        }
        if(exe != null){
            exe.shutdown();
        }
        while (true) {
            System.out.println("cyclic......");
            if (exe.isTerminated()) {
                break;
            }
        }
        System.out.println("running2 end......");
    }


    @Test
    public void test2(){
//        ExecutorService exe = Executors.newFixedThreadPool(5);
        for (int i = 1; i <= 10; i ++){
            int finalI = i;
            exe.execute(
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(5000);
                                System.out.println(Thread.currentThread().getName() + " number " + finalI);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    })
            );
        }
        if(exe != null){
            exe.shutdown();
        }
        while (true) {
            System.out.println("cyclic......");
            if (exe.isTerminated()) {
                break;
            }
        }
        System.out.println("running end......");
    }

    @Test
    public void threadTest(){
        Thread thread = new Thread(){
            @Override
            public void run() {
//                System.out.println("runing");
                log.info("running");
            }
        };
        thread.start();
//        System.out.println("running");
        log.info("running");
    }
}
