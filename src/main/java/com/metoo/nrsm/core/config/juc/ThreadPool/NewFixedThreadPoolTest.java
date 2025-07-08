package com.metoo.nrsm.core.config.juc.ThreadPool;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class NewFixedThreadPoolTest {

    public static void main(String[] args) {
        int[] arr = new int[]{1, 2, 3, 4, 5};
//        List<Integer> list = Arrays.stream(arr).boxed().collect(Collectors.toList());
        List<Integer> list = Arrays.stream(arr).mapToObj(Integer::valueOf).collect(Collectors.toList());
        List items = new ArrayList();
        ExecutorService exe = Executors.newFixedThreadPool(list.size());
        for (Integer item : list) {
            exe.execute(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sleep(15000);
                        System.out.println("item：" + item + " - " + Thread.currentThread().getName());
                        items.add(item);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
        if (exe != null) {
            exe.shutdown();
        }
        while (true) {
            if (exe.isTerminated()) {
                System.out.println("结束");
                System.out.println(items);
                break;
            }
        }
    }

    @Test
    public void test() {
        int[] arr = new int[]{1, 2, 3, 4, 5};
//        List<Integer> list = Arrays.stream(arr).boxed().collect(Collectors.toList());
        List<Integer> list = Arrays.stream(arr).mapToObj(Integer::valueOf).collect(Collectors.toList());
        List items = new ArrayList();
        ExecutorService exe = Executors.newFixedThreadPool(list.size());
        for (Integer item : list) {
            exe.execute(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sleep(15000);
                        System.out.println("item：" + item + " - " + Thread.currentThread().getName());
                        items.add(item);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
        if (exe != null) {
            exe.shutdown();
        }
        while (true) {
            if (exe.isTerminated()) {
                System.out.println("结束");
                System.out.println(items);
                break;
            }
        }
    }
}
