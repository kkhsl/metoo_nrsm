package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.service.IGatherService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMacUtils;
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
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private GatherMacUtils gatherMacUtils;
    @Autowired
    private IGatherService gatherService;


    @GetMapping("mac")
    private void mac(Date date) {
        Long time=System.currentTimeMillis();
        log.info("mac Start......");
        try {
            this.gatherService.gatherMac(DateTools.gatherDate());
        } catch (Exception e) {
            log.error("Error occurred during MAC", e);
        }
        log.info("mac End......" + (System.currentTimeMillis()-time));
    }

    @GetMapping("updateTerminalInfo")
    private void updateTerminalInfo(Date date) {
        try {

            gatherMacUtils.copyGatherData(date);

            terminalService.syncTerminal(date);
            terminalService.updateVMHostDeviceType();
            terminalService.updateVMDeviceType();
            terminalService.updateVMDeviceIp();
            networkElementService.updateObjDisplay();
        } catch (Exception e) {
            log.error("Error while updating terminal information", e);
        }
    }

}
