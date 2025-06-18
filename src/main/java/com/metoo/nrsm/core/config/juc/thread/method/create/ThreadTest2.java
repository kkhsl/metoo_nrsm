package com.metoo.nrsm.core.config.juc.thread.method.create;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ThreadTest2 {

    Logger log = LoggerFactory.getLogger(ThreadTest2.class);

    public static void main(String[] args) {

        System.out.println("JVM启动main线程，main线程执行main方法：打印当前线程名称" + Thread.currentThread().getName());
        // 创建子线程
        MyThread myThread = new MyThread();
        // 启动线程
//        myThread.run();
        // 启动线程
//        myThread.start();
        MyThread myThread1 = new MyThread();
        myThread1.run();
        System.out.println("线程后的其他方法");
    }

    /**
     * 测试主线程等待子线程执行结束
     */
    @Test
    public void test(){
        ExecutorService exe = Executors.newFixedThreadPool(5);
        for (int i = 1; i <= 10; i ++){
            int finalI = i;
            exe.execute(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(4000);
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


    // 测试 synchronized，内部并发执行，未结束，是否会释放锁
    @Test
    public void test2() throws InterruptedException {

        Object obj = new Object();

        Thread t1 = new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                synchronized (obj){


//                    Thread.sleep(2000);


                    log.info("running......");


                    for (int i = 0; i < 5; i++) {
                        Thread t3 = new Thread(new Runnable() {
                            @SneakyThrows
                            @Override
                            public void run() {

                                synchronized (obj){

                                    Thread.sleep(1000);

                                    log.info("t3 running......");
                                }

                            }
                        });
                        t3.start();
                    }
                }
            }
        });

        t1.start();


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (obj){
                    log.info("t2 running......");
                }
            }
        });

        t2.start();


        t1.join();
        t2.join();


        Thread.sleep(20000);

    }
}
