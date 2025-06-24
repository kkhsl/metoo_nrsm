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
