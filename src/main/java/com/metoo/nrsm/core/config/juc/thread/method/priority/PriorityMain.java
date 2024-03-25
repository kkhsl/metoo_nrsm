package com.metoo.nrsm.core.config.juc.thread.method.priority;

/**
 * 优先级
 */
public class PriorityMain {

    public static void main(String[] args) {

        PriorityThreadC c = new PriorityThreadC();
//        c.setPriority(2);
        c.start();

        PriorityThreadA a = new PriorityThreadA();
//        a.setPriority(1);
        a.run();

        PriorityThreadB b = new PriorityThreadB();
//        b.setPriority(10);
        b.run();


    }
}
