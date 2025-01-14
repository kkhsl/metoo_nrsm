package com.metoo.nrsm.core.utils.gather.thread;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.*;

public class GatherDataThreadPool2 {



    public static final int POOL_SIZE;

    static {
        POOL_SIZE = Integer.max(Runtime.getRuntime().availableProcessors(), 15);
    }

    public static void main(String[] args) {
        int s =  Runtime.getRuntime().availableProcessors();
        int POOL_SIZE = Integer.max(Runtime.getRuntime().availableProcessors(), 20);
    }

    // ExecutorService fixedThreadPool = Executors.newSingleThreadExecutor();// 创建单线程池

    // 不推荐使用，这种方式对现成的控制粒度比较低
    ExecutorService fixedThreadPool = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);//定长线程池

//     ExecutorService fixedThreadPool = Executors.newCachedThreadPool();//


    private static GatherDataThreadPool pool = new GatherDataThreadPool();// 创建单例

    /**
     * 获取一个单例
     * @return
     */
    public static GatherDataThreadPool getInstance(){
        return pool;
    }


    /**
     * 向线程池添加一个任务
     * submit(): 向线程池提交单个异步任务
     * invokeAll(): 向线程池提交批量异步任务
     * @param run
     */
    public void addThread(Runnable run) {
        fixedThreadPool.execute(run);
    }

    /**
     * @description 获取线程池对象
     * @return
     */
    public ExecutorService getService() {
        return fixedThreadPool;
    }


    public ExecutorService getFixedThreadPool(int size){
        return Executors.newFixedThreadPool(size);
    }


}


class Test{

    public void test(){
        // 推荐手动创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                4,
                10,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("t1");
                        return t;
                    }
                }, new ThreadPoolExecutor.AbortPolicy());
        // 线程池执行任务
        executor.execute(() ->{
            for (int i = 0; i <= 10; i++){
                System.out.println(i);
            }
        });
    }
}
