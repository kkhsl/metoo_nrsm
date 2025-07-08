package com.metoo.nrsm.core.config.juc.future;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Slf4j
public class MyFutureTask {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        FutureTask<Integer> futrueTask = new FutureTask(new Callable() {
            @Override
            public Object call() throws Exception {

                Thread.sleep(1000);

                log.info("running......");

                return 100;
            }
        });


        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("running4......");
            }
        };


        Thread t1 = new Thread(futrueTask, "t1");
        t1.start();

        log.info(String.valueOf(futrueTask.get()));// 主线程阻塞

        log.info("123");

    }


}
