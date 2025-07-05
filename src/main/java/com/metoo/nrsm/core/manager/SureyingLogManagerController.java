package com.metoo.nrsm.core.manager;


import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ISurveyingLogService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.vo.SurveyingLogVo;
import com.metoo.nrsm.entity.SurveyingLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequestMapping("/admin/sureying/log")
@RestController
public class SureyingLogManagerController {

    @Autowired
    private ISurveyingLogService surveyingLogService;

    // false true结束轮询
    @GetMapping
    public Result logs() {
        // 获取测绘日志
        List<SurveyingLogVo> surveyingLogList = surveyingLogService.queryLogInfo();
        Map result = new HashMap();
        boolean finish = false;
        result.put("data", surveyingLogList);

        if (surveyingLogList.size() > 0) {
            long count = surveyingLogList.stream().filter(surveyingLogVo -> surveyingLogVo.getType() != null
                    && surveyingLogVo.getType() == 3 && (surveyingLogVo.getStatus() == 2 || surveyingLogVo.getStatus() == 3)).count();
            if (count >= 1) {
                finish = true;
            }
        }
        result.put("finish", finish);
        return ResponseUtil.ok(result);
    }

//    @GetMapping(value = "/logs-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamLogs() {
//            SseEmitter emitter = new SseEmitter(60_000L); // 60秒超时
//        AtomicBoolean isCompleted = new AtomicBoolean(false); // 添加状态标志
//
//        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//
//        Runnable checkLogsTask = () -> {
//            try {
//                // 使用状态标志替代 isComplete() 检查
//                if (isCompleted.get()) {
//                    scheduler.shutdown();
//                    return;
//                }
//
//                List<SurveyingLog> surveyingLogList = surveyingLogService.selectObjByMap(null);
//                boolean finish = false;
//
//                if (surveyingLogList != null && !surveyingLogList.isEmpty()) {
//                    finish = surveyingLogList.stream()
//                            .anyMatch(log -> log.getType() != null
//                                    && log.getType() == 4 && (log.getStatus() == 2 || log.getStatus() == 3));
//                }
//
//                emitter.send(SseEmitter.event()
//                        .data(surveyingLogList)
//                        .id(String.valueOf(System.currentTimeMillis()))
//                        .name("log-update"));
//
//                if (finish) {
//                    isCompleted.set(true); // 更新状态标志
//                    emitter.complete();
//                    scheduler.shutdown();
//                }
//            } catch (Exception e) {
//                isCompleted.set(true); // 出错时也更新状态
//                emitter.completeWithError(e);
//                scheduler.shutdown();
//            }
//        };
//
//        scheduler.scheduleAtFixedRate(checkLogsTask, 0, 2, TimeUnit.SECONDS);
//
//        emitter.onCompletion(() -> {
//            isCompleted.set(true);
//            scheduler.shutdown();
//            System.out.println("SSE completed");
//        });
//
//        emitter.onTimeout(() -> {
//            isCompleted.set(true);
//            scheduler.shutdown();
//            System.out.println("SSE timed out");
//        });
//
//        return emitter;
//    }

    @GetMapping(value = "/logs-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLogs() {
        SseEmitter emitter = new SseEmitter(60_000L); // 60秒超时
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // 用于保存上一次发送的日志列表的哈希值
        AtomicInteger lastHash = new AtomicInteger(0);

        Runnable checkLogsTask = () -> {
            try {
                if (isCompleted.get()) {
                    scheduler.shutdown();
                    return;
                }

                List<SurveyingLog> surveyingLogList = surveyingLogService.selectObjByMap(null);
                boolean finish = false;

                if (surveyingLogList != null && !surveyingLogList.isEmpty()) {
                    finish = surveyingLogList.stream()
                            .anyMatch(log -> log.getType() != null
                                    && log.getType() == 5 && (log.getStatus() == 2 || log.getStatus() == 3));
                }

                // 计算当前日志列表的哈希值
                int currentHash = surveyingLogList != null ? surveyingLogList.hashCode() : 0;

                // 只有当列表发生变化时才发送
                if (currentHash != lastHash.get()) {
                    emitter.send(SseEmitter.event()
                            .data(surveyingLogList)
                            .id(String.valueOf(System.currentTimeMillis()))
                            .name("log-update"));

                    lastHash.set(currentHash); // 更新哈希值
                }

                if (finish) {
                    isCompleted.set(true);
                    emitter.complete();
                    scheduler.shutdown();
                }
            } catch (Exception e) {
                isCompleted.set(true);
                emitter.completeWithError(e);
                scheduler.shutdown();
            }
        };

        scheduler.scheduleAtFixedRate(checkLogsTask, 0, 2, TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            isCompleted.set(true);
            scheduler.shutdown();
            System.out.println("SSE completed");
        });

        emitter.onTimeout(() -> {
            isCompleted.set(true);
            scheduler.shutdown();
            System.out.println("SSE timed out");
        });

        return emitter;
    }


    @GetMapping("/sse/time")
    public void streamTime(HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            int eventId = 0;
            while (!Thread.currentThread().isInterrupted()) {
                String now = LocalTime.now().toString();
                writer.write("id: " + eventId + "\n");
                writer.write("event: timeUpdate\n");
                writer.write("data: 当前时间是 " + now + "\n\n");
                writer.flush();

                eventId++;

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

}
