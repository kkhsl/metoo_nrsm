package com.metoo.nrsm.core.config.ssh.ssh.service;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import com.jcraft.jsch.Channel;

public interface WebSSHService {
    void initConnection(WebSocketSession session);

    void recvHandle(String buffer, WebSocketSession session);

    void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;

    void close(WebSocketSession session);

    void transToSSH(Channel channel, String command) throws IOException;
}
