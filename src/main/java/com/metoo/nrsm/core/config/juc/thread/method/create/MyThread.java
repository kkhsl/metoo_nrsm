package com.metoo.nrsm.core.config.juc.thread.method.create;

public class MyThread extends Thread {

    public MyThread() {
        System.out.println("构造方法：打印当前线程的名称：" + Thread.currentThread().getName());
    }

    @Override
    public void run() {
        // 休眠5秒
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("自定义Thread实现类：打印当前线程名称：" + Thread.currentThread().getName());
    }

}
