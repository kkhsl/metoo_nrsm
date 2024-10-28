package com.metoo.nrsm.core.config.sse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-22 11:23
 *
 * Server-Sent Events (SSE) 是一种向客户端推送数据的技术，它允许服务器向客户端单向发送即时更新。客户端通过 EventSource API 接收来自服务器的事件。
 *
 * 除了原生的 SSE 和 WebSocket，还可以考虑使用第三方的实时通信服务，如 Firebase Realtime Database、Pusher、Socket.io 等，
 * 这些服务提供了更高级的实时数据推送功能，并且通常具有更好的跨平台和跨网络支持。
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


// websocket
/*@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MyWebSocketHandler(), "/websocket");
    }
}

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Connection established, send initial data or start pushing data
        session.sendMessage(new TextMessage("Hello, WebSocket!"));
    }
}*/

/*
const socket = new WebSocket('ws://localhost:8080/websocket');

        socket.onmessage = function(event) {
        console.log('Message from server:', event.data);
        };

        socket.onopen = function(event) {
        console.log('WebSocket connection opened.');
        };
*/

