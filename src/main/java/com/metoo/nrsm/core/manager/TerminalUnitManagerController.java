package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ITerminalUnitService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.TerminalUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/terminal/unit")
public class TerminalUnitManagerController {

    @Autowired
    private ITerminalUnitService terminalUnitService;

    @GetMapping("/all")
    public Result all(){
        List<TerminalUnit> terminalUnitList = this.terminalUnitService.selectObjAll();
        return ResponseUtil.ok(terminalUnitList);
    }

}
