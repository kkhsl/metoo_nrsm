package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ITerminalDiagnosisService;
import com.metoo.nrsm.core.vo.Result;
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

    @GetMapping
    public Result diagnosis(){
        TerminalDiagnosis terminalDiagnosis = terminalDiagnosisService.selectObjByType(1);
        return ResponseUtil.ok(terminalDiagnosis);
    }
}
