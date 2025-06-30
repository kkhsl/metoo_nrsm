package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ITerminalDiagnosisService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.service.impl.TerminalDiagnosisImpl;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalDiagnosis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/admin/terminal/diagnosis")
public class TerminalDiagnosisController {


    @Autowired
    private ITerminalDiagnosisService terminalDiagnosisService;

    @Autowired
    private TerminalDiagnosisImpl terminalDiagnosis;

    @Autowired
    private ITerminalService terminalService;

    private final ReentrantLock lock = new ReentrantLock();

    private final ExecutorService executor = Executors.newCachedThreadPool();
    //
    @GetMapping(
            value = "${sse.endpoint.terminal}/{terminalId}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter handleTerminalSse(
            @PathVariable("terminalId") String terminalId) {

        SseEmitter emitter = new SseEmitter(0L); // 无超时限制
        if (lock.tryLock()) {
            try {
                // 先发送正在请求提示
                String startInfo = "{\"conversationId\":\"\",\"data\":{\"type\":\"http\"},\"event\":\"FLOW_STARTED\",\"requestId\":\"\",\"topicId\":\"\"}";
                try {
                    emitter.send(startInfo);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                    return emitter;
                }
                executor.execute(() -> {
                    try {
                        terminalDiagnosis.processSseStream(terminalId)
                                .subscribe(
                                        data -> {
                                            try {
                                                emitter.send(data);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        },
                                        error -> emitter.completeWithError(error),
                                        () -> emitter.complete()
                                );
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                });
                // 客户端断开时的清理
                emitter.onCompletion(() -> {
                    //
                });
                emitter.onTimeout(() -> {
                    //
                });
            }finally {
                lock.unlock();
            }
        }
        return emitter;
    }



    @GetMapping
    public Result diagnosis(String terminalId){
        Terminal terminal = this.terminalService.selectObjById(Long.parseLong(terminalId));
        if(terminal != null){
            TerminalDiagnosis terminalDiagnosis = terminalDiagnosisService.selectObjByType(terminal.getConfig());
            if(terminalDiagnosis != null){
                terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{IPv4}", terminal.getV4ip() != null ? terminal.getV4ip() : ""));
                terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{IPv4_subnet}", terminal.getPortSubne() != null ? terminal.getPortSubne() : ""));

                terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{Interface}", terminal.getPortName() != null ? terminal.getPortName() : ""));
                terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{Vendor}", terminal.getVendor() != null ? terminal.getVendor() : ""));

                String portIpv6Subnet = terminal.getPortIpv6Subnet();
                if(portIpv6Subnet != null && !portIpv6Subnet.isEmpty()){
                    if(portIpv6Subnet.contains("/")){
                        String ip = portIpv6Subnet.split("/")[0];
                        String mask = portIpv6Subnet.split("/")[1];
                        terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{IPv6}", ip));
                        terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{Prefix-length}", mask));
                    }
                }
                return ResponseUtil.ok(terminalDiagnosis);
            }
        }
       return ResponseUtil.ok();
    }





}
