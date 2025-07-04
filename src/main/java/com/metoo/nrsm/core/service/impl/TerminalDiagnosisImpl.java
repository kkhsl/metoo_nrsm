package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.NetworkDataMapper;
import com.metoo.nrsm.core.mapper.TerminalDiagnosisMapper;
import com.metoo.nrsm.core.service.ITerminalDiagnosisService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.entity.AiDifyRequest;
import com.metoo.nrsm.entity.AiRequest;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalDiagnosis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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

    @Value("${ai.difyAi.url}")
    private String difyUrl;

    @Value("${ai.difyAi.token}")
    private String token;

    public Flux<String> processSseStream(String terminalId) {
        Terminal terminal = terminalService.selectObjById(Long.parseLong(terminalId));
        // 2. 获取IPv4地址
        String ipv4Address = terminal.getV4ip();
        AiRequest requestBody = buildAiRequest(ipv4Address);
        WebClient webClient = WebClient.create(aiOriginalUrl);
        return webClient.post()
                .uri("/aiSend")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(e -> Flux.just("event: error\ndata: " + e.getMessage()));
    }

    private AiRequest buildAiRequest(String ipv4) {
        AiRequest request = new AiRequest(ipv4);
        request.addData("ipv6端口表", networkDataMapper.getIpv6PortTable());
        request.addData("终端表", networkDataMapper.getTerminalTable());
        request.addData("ipv6出口是否通表", networkDataMapper.getIpv6Connectivity());
        request.addData("端口表", networkDataMapper.getPortTable());
        return request;
    }

    @Override
    public TerminalDiagnosis selectObjByType(Integer type) {
        return this.terminalDiagnosisMapper.selectObjByType(type);
    }


    /**
     * 构建请求参数
     *
     * @param ipv4
     * @return
     */
    private AiDifyRequest buildDifyAiRequest(String ipv4) {
        AiDifyRequest vo = new AiDifyRequest();
        vo.addData("input_text", "根据ipv6改造建议的文档逻辑，结合终端表、端口表、ipv6端口表、动态内容表、出口ipv6是否通表。结合给出ipv4地址为" + ipv4 + "的最终结果");
        vo.addData("Multisentiment", "True");
        vo.addData("terminal", networkDataMapper.getTerminalTable());
        vo.addData("ipv6Port", networkDataMapper.getIpv6PortTable());
        vo.addData("ping", networkDataMapper.getIpv6Connectivity());
        vo.addData("port", networkDataMapper.getPortTable());
        return vo;
    }

    /**
     * ipv改造建议
     *
     * @param terminalId
     * @return
     */
    public Flux<String> ipv6Suggestion(String terminalId) {
        Terminal terminal = terminalService.selectObjById(Long.parseLong(terminalId));
        // 2. 获取IPv4地址
        String ipv4Address = terminal.getV4ip();
        AiDifyRequest requestBody = buildDifyAiRequest(ipv4Address);
        WebClient webClient = WebClient.create(difyUrl);
        String tokenKey = "Bearer " + token;
        return webClient.post()
                .uri("/run")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", tokenKey)
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(e -> Flux.just("event: error\ndata: " + e.getMessage()));
    }


}
