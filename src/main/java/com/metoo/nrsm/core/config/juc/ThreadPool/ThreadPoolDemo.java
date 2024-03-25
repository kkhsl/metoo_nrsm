package com.metoo.nrsm.core.config.juc.ThreadPool;

import com.metoo.nrsm.core.config.juc.callable.TestCallable;
import com.metoo.nrsm.core.config.juc.callable.TestCallable2;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@RequestMapping("/threadpool")
@RestController
public class ThreadPoolDemo {

    private static Logger log = LoggerFactory.getLogger(ThreadPoolDemo.class);

    private static int POOL_SIZE = Integer.max(Runtime.getRuntime().availableProcessors(), 0);
    private static ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);

    public static void main(String[] args) {
//        for (int i = 1; i <= 1; i++){
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    test2();
//                }
//            }).start();
//
////            new Thread(new Runnable() {
////                @Override
////                public void run() {
////                    test2();
////                }
////            }).start();
////
////            new Thread(new Runnable() {
////                @Override
////                public void run() {
////                    test1();
////                }
////            }).start();
//
//            log.info("for"+i+"程结束");
//        }
//        log.info("主线程结束");

        test2();
    }

    public static void test1(){
        for (int i = 1; i <= 10; i++){
            exe.execute(new Runnable() {
                @Override
                public void run() {

                    System.out.println("Thread-1:");
                }
            });
        }
    }

    public static void test2(){

        int POOL_SIZE = Integer.max(Runtime.getRuntime().availableProcessors(), 0);
        ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);


        AtomicInteger num =  new AtomicInteger();
        AtomicInteger num1 =  new AtomicInteger();
        List list = new ArrayList();
        for (int i = 1; i <= 20; i++){
            exe.execute(new Runnable() {
                @Override
                public void run() {
//                    try {
//                        Thread.sleep(1000);
//                    synchronized (this){
//                        list.add(num1.getAndIncrement());
//                    }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

//                    synchronized (this){
//                        list.add(1);
//                    }

                    list.add(1);

                    System.out.println("Thread-2: " + num.getAndIncrement());
                }
            });
        }

        if(exe != null){
            exe.shutdown();
        }
        while (true){
            if (exe.isTerminated()) {
                System.out.println("关闭");
                System.out.println(list);
                break;
            }
        }
    }

    @GetMapping("/test3")
    public void test3(){
        List list = new ArrayList();
        for (int i = 1; i <= 10; i++){
            list.add(i);
            System.out.println("Thread-2: " + i);
        }
        System.out.println(list);
    }


    @Test
    public void test4(){
        AtomicInteger num =  new AtomicInteger();
        AtomicInteger num1 =  new AtomicInteger();
        List list = new ArrayList();
        for (int i = 1; i <= 3; i++){
            exe.execute(new Runnable() {
                @Override
                public void run() {
//                    try {
//                        Thread.sleep(1000);
//                    synchronized (this){
//                        list.add(num1.getAndIncrement());
//                    }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

//                    synchronized (this){
//                        list.add(1);
//                    }

                    list.add(1);

                    System.out.println("Thread-2: " + num.getAndIncrement());
                }
            });
        }

        if(exe != null){
            exe.shutdown();
        }
        while (true){
            if (exe.isTerminated()) {
                System.out.println("关闭");
                System.out.println(list);
                break;
            }
        }
    }


    /**
     * 遍历创建线程池，查看线程的名称
     */
    @Test
    public void test5(){
        ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);
        List list = new ArrayList();
        for (int i = 1; i <= 2; i++){
            System.out.println("循环 " + i);
            final CountDownLatch countDownLatch = new CountDownLatch(2);

            for (int j = 0; j < 10; j++) {
                Future<Integer> future = exe.submit(new TestCallable(j));
                try {
                    list.add(future.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                System.out.println("二层循环" + j);
                countDownLatch.countDown();
            }
            try {
                countDownLatch.await();
                System.out.println("等待结束" + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                System.out.println(list);
            }
        }
    }


    @Test
    public void test6() throws ExecutionException, InterruptedException {
        ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);

        for (int i = 1; i <= 2; i++){
            System.out.println("循环 " + i);
            final CountDownLatch countDownLatch = new CountDownLatch(2);

            for (int j = 0; j < 10; j++) {
                Future<Object> future = exe.submit(new TestCallable2());

                future.get();

                System.out.println("二层循环" + j);
                countDownLatch.countDown();
            }
            try {
                countDownLatch.await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                System.out.println("等待结束" + i);
            }
        }
    }

    @Test
    public void test7(){
        ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);
        List list = new ArrayList();
        //Future 相当于是用来存放Executor执行的结果的一种容器
        ArrayList<Future<Integer>> results = new ArrayList<Future<Integer>>();

        for (int i = 1; i <= 2; i++){
            System.out.println("循环 " + i);
            final CountDownLatch countDownLatch = new CountDownLatch(2);

            for (int j = 0; j < 10; j++) {
//                Future<Integer> future = exe.submit(new TestCallable(j));
                results.add(exe.submit(new TestCallable(j)));
                System.out.println("二层循环" + j);
                countDownLatch.countDown();
            }
            try {


                countDownLatch.await();

                for (Future<Integer> fs : results) {
                    if (fs.isDone()) {
                        list.add(fs.get());
                    } else {
                        System.out.println("Future result is not yet complete");
                    }
                }

                System.out.println("等待结束" + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } finally {
                System.out.println(list);
            }
        }
    }

    // 测试使用线程池，每个线程对list集合操作两次添加动作
    @Test
    public void test8(){
        ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);
        List<Integer> list = new ArrayList();
        for (int i = 0; i < 5000000; i++) {
            exe.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (list){
                        list.add(1);
                        list.add(1);

                    }
                }
            });
        }
        try {
            Thread.currentThread().join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int sum =list.stream()
                .mapToInt(e -> e).sum();
        System.out.println(sum);
    }

    @Test
    public void test9(){
        ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);
        List<Integer> list = new Vector<>();
        List<Integer> num = new ArrayList();
        for (int i = 0; i < 5000000; i++) {
            num.add(i);
        }
        final CountDownLatch countDownLatch = new CountDownLatch(5000000);

        num.stream().forEach(e ->{
            exe.execute(new Runnable() {
                @Override
                public void run() {
                    list.add(1);   list.add(1);
                    countDownLatch.countDown();
                }
            });
        });
        try {
            countDownLatch.await();
            int sum =list.stream()
                    .mapToInt(e -> e).sum();
            System.out.println(sum);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test10(){
        ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);
        List<Integer> list = new Vector<>();
        List<Integer> num = new ArrayList();
        for (int i = 0; i < 500000000; i++) {
            num.add(i);
        }
        final CountDownLatch countDownLatch = new CountDownLatch(500000000);

        num.parallelStream().forEach(e ->{
            exe.execute(new Thread(new Runnable() {
                @Override
                public void run() {
                    list.add(1);
                    countDownLatch.countDown();
                }
            }));
         });
        try {
            countDownLatch.await();
            int sum =list.stream()
                    .mapToInt(e -> e).sum();
            System.out.println(sum);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}