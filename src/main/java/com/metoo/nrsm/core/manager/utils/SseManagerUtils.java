package com.metoo.nrsm.core.manager.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class SseManagerUtils {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    private ObjectMapper objectMapper;

    public void addEmitter(String sessionId, SseEmitter emitter) {
        emitter.onCompletion(() -> removeEmitter(sessionId));
        emitter.onTimeout(() -> removeEmitter(sessionId));
        emitters.put(sessionId, emitter);
    }

    private void removeEmitter(String sessionId) {
        emitters.remove(sessionId);
    }

    public void sendLogToAll(String taskType, String logMessage) {
        if (emitters.isEmpty()) return;

        executor.execute(() -> {
            String structuredLog = createLogMessage(taskType, logMessage);

            emitters.forEach((sessionId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event().data(structuredLog));
                } catch (Exception e) {
                    removeEmitter(sessionId);
                    log.warn("Client disconnected: {}", sessionId);
                }
            });
        });
    }

    private String createLogMessage(String taskType, String logMessage) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        Map<String, String> logData = new HashMap<>();
        logData.put("type", taskType);
        logData.put("time", timestamp);
        logData.put("message", logMessage);

        try {
            return objectMapper.writeValueAsString(logData);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize log message", e);
            return "{}";
        }
    }
}
