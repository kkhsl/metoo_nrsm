package com.metoo.nrsm.core.config.juc.ThreadPool;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * 模拟数列求和
 */
public class MyForkJoinPool {

    // 计算数列的和，需要返回结果，可以定义任务继承RecursiveTask
    private static class CounTask extends RecursiveTask<Long> {
        // 定义数据规模的阈值，允许计算10000个数内的和，超过该阈值的数列就需要分解
        private static final int THRESHOLD = 10000;

        private static final int TASKNUM = 100;// 定义每次把大任务分解为100个小任务

        private long start;// 计算数列的起始值
        private long end;// 计算数列的结束之

        public CounTask(long start, long end) {
            this.start = start;
            this.end = end;
        }

        // 重写RecursiveTask类的compute()方法，计算列的结果
        @Override
        protected Long compute() {
            long sum = 0;// 保存计算的结果
            // 判断任务是否需要分解，如果当前数列的end与start范围的数超过阈值的THRESHOLD，就需要分解
            if (end - start < THRESHOLD) {
                // 小于阈值可以直接计算
                for (long i = start; i < end; i++) {
                    sum += i;
                }
            } else {
                // 范围超过阈值，继续分级
                // 约定每次分解成100个小任务，计算每个任务的计算量
                long step = (start + end) / TASKNUM;
                // start=0, end = 200000, step = 2000, 如果计算【0，200000】范围内的和，把该范围的数列分解为100个小任务，每个任务计算2000个数即可
                // 注意，如果任务划分的层次很深，既THRESHOLD阈值太小，每个任务的计算量很小，层次划分很深，可能出现两种情况
                // 一：系统内的线程数量会越积越多，导致性能下降严重；
                // 二：分解次数过多，方法调用过多可能会导致栈溢出
                // 创建一个存储任务的集合
                ArrayList<CounTask> list = new ArrayList<>();
                long pos = start;// 每个任务的起始位置
                for (int i = 0; i < TASKNUM; i++) {
                    long lastOne = pos + step;// 每个任务的结束位置
                    // 调整最后一个任务的结束位置
                    if (lastOne > end) {
                        lastOne = end;
                    }
                    // 创建一个子任务
                    CounTask counTask = new CounTask(pos, lastOne);
                    // 把任务添加到集合中
                    list.add(counTask);
                    // 调用fork()提交子任务
                    counTask.fork();
                    // 调整下个任务的起始位置
                    pos += step + 1;
                }
                // 等待所有子任务结束后，合并计算结果
                for (CounTask counTask : list) {
                    sum += counTask.join();// join会一直等待子任务执行完毕返回执行结果
                }
            }
            return sum;
        }
    }

    public static void main(String[] args) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        CounTask counTask = new CounTask(0L, 200000L);
        ForkJoinTask<Long> result = forkJoinPool.submit(counTask);
        try {
            Long sum = result.get();
            System.out.println("计算数列结果为：" + sum);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        long m = 0L;
        for (long i = 0; i <= 200000; i++) {
            m += i;
        }
        System.out.println(m);
    }
}
