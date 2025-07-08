package com.metoo.nrsm.core.config.juc.thread.method.priority;

import lombok.SneakyThrows;

public class PriorityThreadC extends Thread {

    @SneakyThrows
    @Override
    public void run() {
        long begin = System.currentTimeMillis();
        int sum = 0;
        for (int i = 0; i <= 10; i++) {
            sum += i;
            System.out.println("C: " + Thread.currentThread().getName() + " num: " + i);
            Thread.sleep(1000);
        }
        long end = System.currentTimeMillis();
        System.out.println("Thread C " + (end - begin));
    }
}
