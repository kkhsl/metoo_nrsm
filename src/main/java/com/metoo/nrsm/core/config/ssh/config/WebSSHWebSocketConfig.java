package com.metoo.nrsm.core.config.ssh.config;

import com.metoo.nrsm.core.config.ssh.interceptor.WebSocketInterceptor;
import com.metoo.nrsm.core.config.ssh.ssh.websocket.WebSSHWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;


/**
 * 配置类（实现WebSocketConfigurer接口 ）
 */
@Configuration
@EnableWebSocket
public class WebSSHWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    WebSSHWebSocketHandler webSSHWebSocketHandler;

    public WebSSHWebSocketConfig() {
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        // //socket通道
        ////指定处理器和路径，并设置跨域
        webSocketHandlerRegistry
                .addHandler(this.webSSHWebSocketHandler, new String[]{"/webssh"})
                .addInterceptors(new HandshakeInterceptor[]{new WebSocketInterceptor()})
                .setAllowedOrigins(new String[]{"*"});
    }
}