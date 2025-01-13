package com.metoo.nrsm.core.config.juc.future;

import com.metoo.nrsm.core.utils.gather.thread.GatherDataThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

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


        Thread t1 = new Thread(futrueTask,"t1");
        t1.start();

        log.info(String.valueOf(futrueTask.get()));// 主线程阻塞

        log.info("123");

    }


    @Test
    public void main2() throws InterruptedException, ExecutionException {

        for (Integer i = 0; i < 2 ; i++) {
            FutureTask<Integer> futrueTask = new FutureTask(new Callable() {
                @Override
                public Object call() throws Exception {

                    for (int j = 0; j <10 ; j++) {
                        System.out.println(Thread.currentThread().getName() + ": " + j);
                    }
                    Thread.sleep(1000);
                    return 100;
                }
            });

            GatherDataThreadPool.getInstance().addThread(futrueTask);
            log.info(String.valueOf(futrueTask.get()));// 主线程阻塞
        }

        log.info("123");
    }


}
