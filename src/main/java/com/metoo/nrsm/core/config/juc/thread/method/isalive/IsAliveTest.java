package com.metoo.nrsm.core.config.juc.thread.method.isalive;

/**
 * 活动状态
 */
public class IsAliveTest {

    public static void main(String[] args) throws InterruptedException {

        IsAliveThread isAliveThread = new IsAliveThread();
        System.out.println("运行前：" + isAliveThread.isAlive());
        isAliveThread.start();
        Thread.sleep(1000);
        System.out.println("运行后：" + isAliveThread.isAlive());


    }
}
