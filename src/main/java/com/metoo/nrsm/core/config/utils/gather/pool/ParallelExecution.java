package com.metoo.nrsm.core.config.utils.gather.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-28 11:58
 */
public class ParallelExecution {

    public static void main(String[] args) {
        // 原始的串行任务
        List<String> data = getData();

        // 使用线程池并行处理任务
        ExecutorService executorService = Executors.newFixedThreadPool(4); // 创建一个固定大小的线程池
        List<Callable<List<String>>> tasks = new ArrayList<>();


        // 拆分任务
        int chunkSize = data.size() / 4; // 假设将数据平均分成4部分
        for (int i = 0; i < 4; i++) {
            final int startIndex = i * chunkSize;
            final int endIndex = (i + 1 == 4) ? data.size() : (i + 1) * chunkSize;
            tasks.add(new Callable<List<String>>() {
                @Override
                public List<String> call() throws Exception {
                    return processSubList(data.subList(startIndex, endIndex));
                }
            });
        }

        // 提交任务并获取结果
        List<Future<List<String>>> futures;
        try {
            futures = executorService.invokeAll(tasks); // 并行执行任务
            executorService.shutdown(); // 关闭线程池
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // 收集结果
        List<String> mergedResult = new ArrayList<>();
        for (Future<List<String>> future : futures) {
            try {
                mergedResult.addAll(future.get()); // 获取每个任务的结果并合并
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // 处理合并后的结果
        System.out.println("Merged Result: " + mergedResult);
    }

    // 模拟获取数据的方法
    private static List<String> getData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            data.add("Data" + i);
        }
        return data;
    }

    // 这个方法在其他类中, 使用static修饰,通过静态方法处理任务
    // 不用静态方法,通过new对象的实例调用
    // 模拟处理子列表的方法
    private static List<String> processSubList(List<String> subList) {
        List<String> processedResult = new ArrayList<>();
        for (String item : subList) {
            // 模拟复杂计算或处理过程
            processedResult.add(item + "_processed");
        }
        return processedResult;
    }

}
