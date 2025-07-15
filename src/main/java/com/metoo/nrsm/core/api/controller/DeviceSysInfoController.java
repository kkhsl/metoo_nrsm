package com.metoo.nrsm.core.api.controller;

import com.metoo.nrsm.core.api.dto.DeviceSysInfoDTO;
import com.metoo.nrsm.core.api.service.IDeviceSysInfoService;
import groovy.transform.SelfType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接收系统数据
 *
 */
@Slf4j
@RequestMapping("/api/v1/device/sys")
@RestController
public class DeviceSysInfoController {

    private final IDeviceSysInfoService deviceSysInfoService;
    @Autowired
    public DeviceSysInfoController(IDeviceSysInfoService deviceSysInfoService){
        this.deviceSysInfoService = deviceSysInfoService;
    }

    @PostMapping("/info")
    public void receiveDeviceInfo(@RequestBody DeviceSysInfoDTO deviceSysInfoDTO) {
        try {
            deviceSysInfoService.insert(deviceSysInfoDTO);
            log.info("Device system info received and saved successfully");

        } catch (Exception e) {
            log.info("Error saving device system info: {}", e.getMessage());
        }
    }


}
