package com.metoo.nrsm.core.config.juc.thread.lock;

import lombok.SneakyThrows;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicTest {

    public static void main(String[] args) {
        // 启用两个线程，不断调用getNum方法
        MyInt myInt = new MyInt();
        for (int i = 0; i < 2; i++){
           new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    while (true){
                        System.out.println(Thread.currentThread().getName() + " - num: " + myInt.getNum());
                        Thread.sleep(300);
                    }
                }
            }).start();
        }

    }

    static class MyInt{
        AtomicInteger num =  new AtomicInteger();
        public int getNum(){
            return num.getAndIncrement();
        }
    }
}
