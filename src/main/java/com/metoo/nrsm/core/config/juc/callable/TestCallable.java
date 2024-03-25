package com.metoo.nrsm.core.config.juc.callable;

import java.util.ArrayList;
import java.util.concurrent.*;

public class TestCallable implements Callable<Integer> {

    private int id;

    public TestCallable(int id) {
        this.id = id;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println(Thread.currentThread().getName());
        Thread.sleep(100);
        return id;
    }

}

class CallableClose {

    public static void main(String[] args) throws InterruptedException,
            ExecutionException {
        ExecutorService exec = Executors.newCachedThreadPool();
        ArrayList<Future<Integer>> results = new ArrayList<Future<Integer>>();    //Future 相当于是用来存放Executor执行的结果的一种容器
        for (int i = 0; i < 10; i++) {
            results.add(exec.submit(new TestCallable(i)));
        }
        for (Future<Integer> fs : results) {
            if (fs.isDone()) {
                System.out.println(fs.get());
            } else {
                System.out.println("Future result is not yet complete");
            }
        }
        exec.shutdown();
    }
}

