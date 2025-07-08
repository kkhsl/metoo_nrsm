package com.metoo.nrsm.core.config.juc.runnerble;

public class RunnbleTest {

    public static void main(String[] args) {
        System.out.println("JVM启动main线程，main线程执行main方法");
        // 创建Runnerble接口的实现类对象
        MyRunnerble myRunnerble = new MyRunnerble();
        // 创建线程对象
        Thread thread = new Thread(myRunnerble);
        // 开启线程
        thread.start();

        for (int i = 0; i <= 10000000; i++) {
            System.out.println("=============" + i);
        }

        // 调用Thread(Runnerble) 构造方法，实参也会传递匿名内部类对象
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 100; i++) {
                    System.out.println("===========anonymous" + i);
                }
            }
        });
        thread1.start();
    }
}
