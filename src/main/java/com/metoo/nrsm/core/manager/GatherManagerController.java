package com.metoo.nrsm.core.manager;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONArray;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.service.impl.ProbeServiceImpl;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMacUtils;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:14
 */
@Slf4j
@RequestMapping("/admin/gather")
@RestController
public class GatherManagerController {

    @Autowired
    private IProbeService probeService;


    private ExecutorService executorService;
    private Future<?> runningTask = null;


    @GetMapping("start")
    public void scanByTerminal(){
        executorService = ThreadUtil.newSingleExecutor();
        Callable<Void> task = () -> {
            if (Thread.currentThread().isInterrupted()) {
                log.error("任务中断");
                return null;
            }
            try {
                gatherMethod();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保留中断状态
                log.error("具体任务中断");
                return null;
            }
            return null;
        };

        runningTask = executorService.submit(task);

        try {
            // 等待任务执行完毕
            runningTask.get();
        } catch (InterruptedException | ExecutionException e) {
            // 处理可能的异常
            log.error("等待测绘任务执行完毕出错：{}", e);
            // 如果被中断，则保留中断状态
            Thread.currentThread().interrupt();
        }
    }


    private void gatherMethod() throws Exception {
        // 具体任务
        probeService.scanByTerminal();
    }

    @GetMapping("/stop")
    public boolean stopGather() {
        if (runningTask != null && !runningTask.isDone()) {
            // 表示如果必要则中断正在运行的线程
            runningTask.cancel(true);
            // 清除引用，以便可以提交新的任务
            runningTask = null;

        }
        executorService.shutdownNow();
        return true;
    }


}

