package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ITerminalDiagnosisService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalDiagnosis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/terminal/diagnosis")
public class TerminalDiagnosisController {

    @Autowired
    private ITerminalDiagnosisService terminalDiagnosisService;
    @Autowired
    private ITerminalService terminalService;

    @GetMapping
    public Result diagnosis(String terminalId){
        Terminal terminal = this.terminalService.selectObjById(Long.parseLong(terminalId));
        if(terminal != null){
            TerminalDiagnosis terminalDiagnosis = terminalDiagnosisService.selectObjByType(terminal.getConfig());

            terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{IPv4}", terminal.getV4ip() != null ? terminal.getV4ip() : ""));
            terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{IPv4_subnet}", terminal.getPortSubne() != null ? terminal.getPortSubne() : ""));

            terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{IPv6}", terminal.getV6ip() != null && !terminal.getV6ip().toLowerCase().startsWith("fe80") ? terminal.getV6ip() : ""));
            terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{Interface}", terminal.getPortName() != null ? terminal.getPortName() : ""));
            terminalDiagnosis.setContent(terminalDiagnosis.getContent().replace("{Vendor}", terminal.getVendor() != null ? terminal.getVendor() : ""));
            terminalDiagnosis.setContent(terminalDiagnosis.getContent().replaceAll("\\r?\\n", "<br/>"));

            return ResponseUtil.ok(terminalDiagnosis);
        }
       return ResponseUtil.ok();
    }
}
