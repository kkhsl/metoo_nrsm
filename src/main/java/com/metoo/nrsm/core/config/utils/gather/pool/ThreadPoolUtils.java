package com.metoo.nrsm.core.config.utils.gather.pool;

import java.util.concurrent.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-28 10:53
 *
 *
 * 线程池关闭。
 *
 * 在Java项目中，如果存在多个定时任务，每个任务需要定期调用线程池执行任务，
 * 通常的做法是使用一个单独的线程池来管理这些定时任务，而不是每个任务都创建一个新的线程池。这样可以更好地管理线程资源和任务执行的调度。
 *
 *
 */
public class ThreadPoolUtils {

    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 50;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<>();

    private static ExecutorService executorService;

    private static volatile boolean canShutdown = false; // 控制线程池关闭的变量

    // 私有化构造函数，防止实例化
    private ThreadPoolUtils() {
    }

    private static ThreadPoolUtils pool = new ThreadPoolUtils();// 创建单例
    /**
     * 获取一个单例
     * @return
     */
    public static ThreadPoolUtils getInstance(){
        return pool;
    }

    // 初始化线程池
    private static void initThreadPool() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = new ThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    MAX_POOL_SIZE,
                    KEEP_ALIVE_TIME,
                    TIME_UNIT,
                    WORK_QUEUE
            );
        }
    }


    // 执行 Runnable 任务
    public static void execute(Runnable runnable) {
        initThreadPool();
        executorService.execute(runnable);
    }

    // 执行 Callable 任务并返回 Future
    public static <T> Future<T> submit(Callable<T> callable) {
        initThreadPool();
        return executorService.submit(callable);
    }

    // 关闭线程池
    public static void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // 等待线程池中的任务执行完成
    public static void awaitTermination() throws InterruptedException {
        if (executorService != null) {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }


    // 关闭线程池,多个定时任务执行时,确保线程池不被意外关闭
}
