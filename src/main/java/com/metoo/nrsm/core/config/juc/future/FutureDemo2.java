package com.metoo.nrsm.core.config.juc.future;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 测试容器中使用future.get()是否会让主线程阻塞
 *
 * 会让主线程阻塞等待，但不是并行执行
 *
 *
 *
 */
@Slf4j
public class FutureDemo2 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

          int POOL_SIZE = Integer.max(Runtime.getRuntime().availableProcessors(), 0);
          ExecutorService staticExe = Executors.newFixedThreadPool(POOL_SIZE);

        for (int i = 0; i < 2; i++){
            Future<Integer> future = null;
            try {
                future = staticExe.submit(new Callable() {
                    @Override
                    public Object call() throws Exception {

                        Thread.sleep(2000);

                        System.out.println("running......" + Thread.currentThread().getName());

                        return 100;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                future.get();
            }
        }

        System.out.println("123");
    }

}
