package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONArray;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.service.impl.ProbeServiceImpl;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMacUtils;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Array;
import java.util.*;

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
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private ProbeServiceImpl probeServiceImpl;

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

            updateTerminalDeviceTypeToNSwitch();

            terminalService.updateVMHostDeviceType();
            terminalService.updateVMDeviceType();


            terminalService.updateVMDeviceIp();
            networkElementService.updateObjDisplay();
        } catch (Exception e) {
            log.error("Error while updating terminal information", e);
        }
    }

    @GetMapping("writeTerminalByProbe")
    public void writeTerminalByProbe(){
        probeServiceImpl.writeTerminal();
    }

    private void updateTerminalDeviceTypeToNSwitch(){
        Map params = new HashMap();
        params.put("deviceType", 1);
        params.put("notDeviceTypeId", 36);
//        params.put("online", true);
        List<Terminal> terminalList = this.terminalService.selectObjByMap(params);
        if(terminalList != null && !terminalList.isEmpty()){
            for (Terminal terminal : terminalList) {
                terminal.setDeviceTypeId(36L);
                this.terminalService.update(terminal);
            }
        }

        List<Long> types = Arrays.asList(37L,16L,19L);
        params.clear();
//        params.put("online", true);
        params.put("notDeviceTypeId", 36);
        List<Terminal> terminals = this.terminalService.selectObjByMap(params);
        if(terminals != null && !terminals.isEmpty()){

            DeviceType deviceType1 = this.deviceTypeService.selectObjByType(14);
            DeviceType deviceType2 = this.deviceTypeService.selectObjByType(27);

            for (Terminal terminal : terminals) {
//                [{"application_protocol":"ssh","port_num":"22"}]
                if(terminal.getCombined_port_protocol() != null && StringUtils.isNotEmpty(terminal.getCombined_port_protocol())){
                    JSONArray jsonArray = JSONArray.parseArray(terminal.getCombined_port_protocol());
                    // 用于存储所有的端口号
                    Set<String> portNumbers = new HashSet<>();

                    // 遍历 JSON 数组，提取所有 port_num
                    for (int i = 0; i < jsonArray.size(); i++) {
                        String portNum = jsonArray.getJSONObject(i).getString("port_num");
                        if (StringUtils.isNotEmpty(portNum)) {
                            portNumbers.add(portNum.trim());  // 将 port_num 添加到集合中
                        }
                    }


                    // 判断是否有 23 端口
                    if (portNumbers.contains("23")) {
                        // 如果包含 23 端口，认为是网络设备，更新设备类型 ID
                        terminal.setDeviceTypeId(37L);
                    } else if (portNumbers.size() > 4) {
                        terminal.setDeviceTypeId(16L);
                    } else  if (portNumbers.contains("22")) {
                        terminal.setDeviceTypeId(16L);
                    } else if (portNumbers.contains("515")) {
                        terminal.setDeviceTypeId(19L);
                    } else {
                        // 恢复为普通终端
                        if(types.contains(terminal.getDeviceTypeId())){
                            terminal.setDeviceTypeId(deviceType1.getId());
                        }
                    }
                    this.terminalService.update(terminal);
                }else{
                    // 恢复为普通终端
                    if(types.contains(terminal.getDeviceTypeId())){
                        terminal.setDeviceTypeId(deviceType1.getId());
                        this.terminalService.update(terminal);
                    }
                }

            }
        }


    }
}
