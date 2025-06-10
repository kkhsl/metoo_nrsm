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
            return ResponseUtil.ok(terminalDiagnosis);
        }
       return ResponseUtil.ok();
    }
}
