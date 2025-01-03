package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.service.ITerminalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:14
 */
@Slf4j
@RequestMapping("/admin/gather")
@RestController
public class GatherManagerController {

    @Autowired
    private ITerminalService terminalService;

    @GetMapping("syncTerminal")
    public void syncTerminal(){
        terminalService.syncTerminal(new Date());
    }

}
