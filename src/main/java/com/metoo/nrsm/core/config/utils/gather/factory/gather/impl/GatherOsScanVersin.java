package com.metoo.nrsm.core.config.utils.gather.factory.gather.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.strategy.Context;
import com.metoo.nrsm.core.config.utils.gather.strategy.other.OsScannerCollectionStrategy;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.service.IProbeService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.file.DataFileWrite;
import com.metoo.nrsm.core.utils.file.FileToDatabase;
import com.metoo.nrsm.core.utils.file.JsonFileToDto;
import com.metoo.nrsm.entity.Probe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 16:37
 */
@Slf4j
@Component
public class GatherOsScanVersin implements Gather {
    /**
     * 创建一个固定大小的线程池
      */
    ExecutorService executorService = ThreadUtil.newExecutor(5);

    public static <T> List<List<T>> splitList(List<T> list, int numOfSubLists) {
        List<List<T>> subLists = new ArrayList<>(numOfSubLists);
        int size = list.size();
        int subListSize = (int) Math.ceil((double) size / numOfSubLists);

        for (int i = 0; i < numOfSubLists; i++) {
            int fromIndex = i * subListSize;
            int toIndex = Math.min(fromIndex + subListSize, size);
            if (fromIndex < toIndex) {
                subLists.add(new ArrayList<>(list.subList(fromIndex, toIndex)));
            } else {
                subLists.add(new ArrayList<>()); // 如果没有更多元素，添加一个空子集合
            }
        }
        return subLists;
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 17; i++) {
            list.add(i);
        }

        List<List<Integer>> result = splitList(list, 5);

//        for (int i = 0; i < result.size(); i++) {
//            System.out.println("Sublist " + (i + 1) + ": " + result.get(i));
//        }

        AtomicInteger index = new AtomicInteger();

        for (List<Integer> integers : result) {

            int i = index.incrementAndGet();

            for (Integer integer : integers) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("Sublist " + String.valueOf(i)
//                        + " ThreadName" + Thread.currentThread().getName()
//                        + ": " + integer);
            }
        }

    }

    @Override
    public void executeMethod() {
        Long time = System.currentTimeMillis();
        log.info("os scan Start......");

        try {

            IProbeService probeService = (IProbeService) ApplicationContextUtils.getBean("probeServiceImpl");
            FileToDatabase fileToDatabase = (FileToDatabase) ApplicationContextUtils.getBean("fileToDatabase");

            Integer number = 5;
            // 清空result_append.txt文件
            try {
                DataFileWrite.clearFile(number);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 创建一个任务列表
            List<Callable<String>> tasks = new ArrayList<>();
            // 分5个列表进行扫描
            Map params = new HashMap();
            params.put("IpaddrvIsNotNull", "IpaddrvIsNotNull");
            List<Probe> probes = probeService.selectObjByMap(params);
            if (probes.size() > 0) {
                // 分5个列表
                List<List<Probe>> splitLists = JsonFileToDto.splitList(probes, number);
                for (int i = 0; i < splitLists.size(); i++) {
                    int finalI = i;
                    tasks.add(() -> {
                        // 扫描执行
                        this.osScan(splitLists.get(finalI), String.valueOf(finalI + 1));
                        return "os scan扫描任务 " + finalI + 1 + " 完成了";
                    });
                }
                try {
                    // 提交所有任务并等待它们完成
                    List<Future<String>> futures = executorService.invokeAll(tasks);
                    for (Future<String> future : futures){
                        String result = future.get();
                        log.info("os-scan任务完成情况========：{}",result);
                    }
                    // 执行下一个读取result_append.txt
                    for (int i = 1; i <= number; i++){
                        // 读取result_append.txt
                        fileToDatabase.readFileToProbe(i+"");
                    }
                } catch (InterruptedException e) {
                   log.error("os scan 任务出现错误:{}",e);
                    // 如果当前线程在等待任务完成时被中断，则需要适当处理
                    Thread.currentThread().interrupt(); // 恢复中断状态
                } finally {
                    // 关闭线程池
                    executorService.shutdown();
                }
            }
        } catch (Exception e) {
            log.error("os scan 出现错误:{}",e);
        }
        log.info("os scan......" + (System.currentTimeMillis() - time));
    }


    public void osScan(List<Probe> probes, String path_suffix) {

        if (probes.size() > 0) {
            PyExecUtils pyExecUtils = (PyExecUtils) ApplicationContextUtils.getBean("pyExecUtils");
            for (Probe obj : probes) {
                if (obj != null) {
                    try {
                        Context context = new Context();
                        context.setAddTime(new Date());
                        context.setEntity(obj);
                        context.setPath(Global.os_scanner + path_suffix);
                        OsScannerCollectionStrategy collectionStrategy = new OsScannerCollectionStrategy(pyExecUtils);
                        collectionStrategy.collectData(context);
//                        // 读取result文件
//                        Device device = null;
//                        params.clear();
//                        params.put("uuid", obj.getDeviceUuid());
//                        List<Device> devices = deviceService.selectObjByMap(params);
//                        if (devices.size() > 0) {
//                            device = devices.get(0);
//                        }
//                        fileToDatabase.write(path_suffix, device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
