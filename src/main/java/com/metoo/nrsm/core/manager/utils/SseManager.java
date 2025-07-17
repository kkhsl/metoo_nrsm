package com.metoo.nrsm.core.manager.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// SSE管理器
@Component
@Slf4j
public class SseManager {
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public void addEmitter(String sessionId, SseEmitter emitter) {
        emitter.onCompletion(() -> removeEmitter(sessionId));
        emitter.onTimeout(() -> removeEmitter(sessionId));
        emitters.put(sessionId, emitter);
    }

    private void removeEmitter(String sessionId) {
        emitters.remove(sessionId);
    }

    // 关键改进：向所有客户端推送任何类型的日志
    public void sendLogToAll(String taskType, String logMessage) {
        if (emitters.isEmpty()) return;

        executor.execute(() -> {
            String structuredLog = createLogMessage(taskType, logMessage);

            emitters.forEach((sessionId, emitter) -> {
                try {
                    emitter.send(structuredLog);
                } catch (Exception e) {
                    removeEmitter(sessionId);
                    log.warn("Client disconnected: {}", sessionId);
                }
            });
        });
    }

    private String createLogMessage(String taskType, String logMessage) {
        // 使用本地化日期时间格式
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

        // 构建带时间信息的JSON
        String json = String.format(
                "{\"type\":\"%s\",\"time\":\"%s\",\"message\":\"%s\"}",
                taskType, timestamp, logMessage.replace("\"", "\\\"")
        );

        // 构建SSE事件
        return "\n" +
                "id: " + UUID.randomUUID() + "\n" +
                "data: " + json + "\n\n";
    }
}