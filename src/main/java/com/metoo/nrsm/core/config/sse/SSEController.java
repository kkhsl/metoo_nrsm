package com.metoo.nrsm.core.config.sse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Server-Sent Events (SSE) 是一种向客户端推送数据的技术，它允许服务器向客户端单向发送即时更新。客户端通过 EventSource API 接收来自服务器的事件。
 */
public class SSEController {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private String message = "Initial message";

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> pushData() {
        executor.scheduleAtFixedRate(() -> {
            // Simulate data generation
            String newData = "Data from server: " + Math.random();
            this.message = newData;
        }, 0, 5, TimeUnit.SECONDS);

        return ResponseEntity.ok()
                .body("data: " + message + "\n\n");
    }
}


