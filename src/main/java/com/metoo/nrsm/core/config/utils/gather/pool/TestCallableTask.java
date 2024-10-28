package com.metoo.nrsm.core.config.utils.gather.pool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-28 11:29
 */
public class TestCallableTask {

    public static void main(String[] args) {

        for (int i = 3; i > 0; i--) {
            MyCallableTask myCallable = new MyCallableTask(i);


            // 提交 Callable 任务，并获取 Future 对象
            Future<String> future = ThreadPoolUtils.submit(myCallable);

            try {
                String result = future.get(); // 阻塞等待任务执行完成并获取结果

                System.out.println("Callable task result: " + result);

                ThreadPoolUtils.shutdown();// 提交任务时,校验线程池是否为空或线程池是否关闭,关闭则重新初始化.所以这里关闭不会影响线程提交

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

    }
}


class MyCallableTask implements Callable {

    private int variable;

    public MyCallableTask(int variable){
        this.variable = variable;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Object call() throws Exception {
        return String.valueOf(variable);
    }
}
