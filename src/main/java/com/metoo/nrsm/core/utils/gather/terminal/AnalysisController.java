package com.metoo.nrsm.core.utils.gather.terminal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisTerminalUtils analysisTerminal;

    // 构造器注入（Spring 会自动注入）
    public AnalysisController(AnalysisTerminalUtils analysisTerminal) {
        this.analysisTerminal = analysisTerminal;
    }

    @GetMapping("/run")
    public String runAnalysis() {
        analysisTerminal.analyze(); // 调用 AnalysisTerminal 的方法
        return "Analysis completed!";
    }
}
