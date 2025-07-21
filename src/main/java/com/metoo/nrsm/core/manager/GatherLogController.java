package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.manager.utils.SseManager;
import com.metoo.nrsm.core.manager.utils.SseManagerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/gather")
public class GatherLogController {

    @Autowired
    private SseManagerUtils sseManagerUtils;


    @GetMapping(path = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllLogs(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        sseManagerUtils.addEmitter(sessionId, emitter);
        return emitter;
    }
}