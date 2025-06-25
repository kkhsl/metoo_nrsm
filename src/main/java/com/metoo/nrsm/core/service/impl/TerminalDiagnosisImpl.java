package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.NetworkDataMapper;
import com.metoo.nrsm.core.mapper.TerminalDiagnosisMapper;
import com.metoo.nrsm.core.service.ITerminalDiagnosisService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.entity.AiRequest;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalDiagnosis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;

@Service
@Transactional
public class TerminalDiagnosisImpl implements ITerminalDiagnosisService {

    @Resource
    private TerminalDiagnosisMapper terminalDiagnosisMapper;

    @Autowired
    private ITerminalService terminalService;

    @Autowired
    private NetworkDataMapper networkDataMapper;

    @Value("${ai.original.url}")
    private String aiOriginalUrl;

    @Autowired
    @Qualifier("sseRestTemplate")  // 注入专门配置的RestTemplate
    private RestTemplate sseRestTemplate;

    public String callAiService(String ip) {
        AiRequest request = buildAiRequest(ip);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "text/event-stream");  // 明确接受SSE流

        HttpEntity<AiRequest> entity = new HttpEntity<>(request, headers);

        try {
            // 使用专用的RestTemplate进行调用
            ResponseEntity<String> response = sseRestTemplate.postForEntity(
                    aiOriginalUrl, entity, String.class);

            return response.getBody();
        } catch (ResourceAccessException e) {
            // 处理超时相关异常
            return "{\"success\":false,\"message\":\"AI服务响应超时\"}";
        } catch (Exception e) {
            // 处理其他异常
            return "{\"success\":false,\"message\":\"AI服务调用失败: " + e.getMessage() + "\"}";
        }
    }

    private AiRequest buildAiRequest(String ipv4) {
        AiRequest request = new AiRequest(ipv4);
        request.addData("ipv6端口表", networkDataMapper.getIpv6PortTable());
        request.addData("终端表", networkDataMapper.getTerminalTable());
        request.addData("ipv6出口是否通表", networkDataMapper.getIpv6Connectivity());
        request.addData("端口表", networkDataMapper.getPortTable());
        return request;
    }


    // SSE事件流处理
    @Async
    public void processSseStream(String terminalId, SseEmitter emitter) {
        try {
            // 1. 获取终端信息
            Terminal terminal = terminalService.selectObjById(Long.parseLong(terminalId));
            sendEvent(emitter, "terminal-info", "获取终端信息成功");

            // 2. 获取IPv4地址
            String ipv4Address = terminal.getV4ip();
            sendEvent(emitter, "ipv4-address", ipv4Address);

            // 3. 调用AI服务
            sendEvent(emitter, "ai-call-start", "开始调用AI服务...");
            String aiResponse = callAiService(ipv4Address);

            // 4. 流式返回AI响应
            sendEvent(emitter, "ai-full-response", aiResponse);

            sendEvent(emitter, "complete", "处理完成");
            emitter.complete();
        } catch (Exception e) {
            sendErrorEvent(emitter, "处理失败: " + e.getMessage());
            emitter.completeWithError(e);
        }
    }

    private void sendEvent(SseEmitter emitter, String name, String data) throws IOException {
        emitter.send(SseEmitter.event()
                .name(name)
                .data(data));
    }

    private void sendErrorEvent(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(message));
        } catch (IOException e) {
            // 忽略发送失败
        }
    }

    @Override
    public TerminalDiagnosis selectObjByType(Integer type) {
        return this.terminalDiagnosisMapper.selectObjByType(type);
    }



}
