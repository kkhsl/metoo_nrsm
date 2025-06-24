package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.MacDTO;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@RequestMapping("/admin/mac")
@RestController
public class MacManagerController {

    @Autowired
    private IMacService macService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IDeviceTypeService deviceTypeService;

    @RequestMapping("de")
    public Result mac_de(){
        List<Mac> deMacList = this.macService.selectTagByDE();

        if(deMacList.size() > 0){
            Map<String, Mac> map = new HashMap<>();
            for (Mac mac : deMacList) {

                String device = mac.getDeviceIp() + mac.getRemoteIp();
                String remote = mac.getRemoteIp() + mac.getDeviceIp();

                if(map.get(device) == null && map.get(remote) == null){
                    map.put(device, mac);
                }
            }
            deMacList.clear();
            for (Map.Entry<String, Mac> stringMacEntry : map.entrySet()) {
                deMacList.add(stringMacEntry.getValue());
            }
        }

        Map params = new HashMap();
        if(deMacList.size() > 0){
            for (Mac DE : deMacList) {
                params.clear();
                if(StringUtil.isNotEmpty(DE.getDeviceIp())){
                    params.put("ip", DE.getDeviceIp());
                    List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
                    if(networkElements.size() > 0){
                        NetworkElement networkElement = networkElements.get(0);
                        // 设置设备Uuid
                        DE.setDeviceUuid(networkElement.getUuid());
                        if(networkElement.getDeviceTypeId() != null){
                            DeviceType deviceType = this.deviceTypeService.selectObjById(networkElement.getDeviceTypeId());
                            DE.setDeviceTypeUuid(deviceType.getUuid());
                            DE.setDeviceType(deviceType.getName());
                        }
                        if(StringUtil.isNotEmpty(DE.getRemoteDevice()) || StringUtil.isNotEmpty(DE.getRemoteIp())){
//                            params.clear();
//                            params.put("hostname", DE.getRemoteDevice());
//                            List<Mac> remoteDeviceList = this.macService.selectObjByMap(params);
                            List<Mac> remoteDeviceList = getMacTagDe(DE.getRemoteDevice(), DE.getRemoteIp());
                            if(remoteDeviceList.size() > 0){
                                Mac remoteDevice = remoteDeviceList.get(0);
                                DE.setRemoteDeviceIp(remoteDevice.getDeviceIp());
                                DE.setRemoteDeviceName(remoteDevice.getDeviceName());
                                params.clear();
                                params.put("deviceIp", DE.getRemoteIp());
                                params.put("remoteIp", DE.getDeviceIp());
                                List<Mac> portMac = this.macService.selectObjByMap(params);
                                if(portMac.size() > 0){
                                    DE.setPort(portMac.get(0).getRemotePort());
                                }
                                if(StringUtil.isNotEmpty(remoteDevice.getDeviceIp())){
                                    params.clear();
                                    params.put("ip", remoteDevice.getDeviceIp());
                                    List<NetworkElement> remote_networkElements = this.networkElementService.selectObjByMap(params);
                                    if(remote_networkElements.size() > 0) {
                                        NetworkElement remote_networkElement = remote_networkElements.get(0);
                                        DE.setRemoteDeviceUuid(remote_networkElement.getUuid());
                                        if (remote_networkElement.getDeviceTypeId() != null) {
                                            DeviceType deviceType = this.deviceTypeService.selectObjById(remote_networkElement.getDeviceTypeId());
                                            DE.setRemoteDevicTypeeUuid(deviceType.getUuid());
                                            DE.setRemoteDeviceType(deviceType.getName());
                                        }
                                    }
                                }

                            }


                        }
                    }
                }
                if(StringUtil.isNotEmpty(DE.getRemoteIp())){
                    params.clear();
                    params.put("deviceIp", DE.getRemoteIp());
                    List<NetworkElement> NSwitch_nes2 = this.networkElementService.selectObjByMap(params);
                    if(NSwitch_nes2.size() > 0){
                        NetworkElement NSwitch_ne = NSwitch_nes2.get(0);
                        DE.setDeviceUuid(NSwitch_ne.getUuid());
                        if (NSwitch_ne.getDeviceTypeId() != null) {
                            DeviceType deviceType = this.deviceTypeService.selectObjById(NSwitch_ne.getDeviceTypeId());
                            DE.setDeviceTypeUuid(deviceType.getUuid());
                            DE.setDeviceType(deviceType.getName());
                        }
                    }
                }
                if(StringUtil.isNotEmpty(DE.getRemoteDevice()) || StringUtil.isNotEmpty(DE.getRemoteIp())){
                    params.clear();
                    params.put("deviceIp", DE.getRemoteIp());
                    List<NetworkElement> NSwitch_nes = this.networkElementService.selectObjByMap(params);
                    if(NSwitch_nes.size() > 0){
                        NetworkElement NSwitch_ne = NSwitch_nes.get(0);
                        DE.setRemoteDeviceUuid(NSwitch_ne.getUuid());
                        DE.setRemoteDeviceIp(NSwitch_ne.getIp());
                        DE.setRemoteDeviceName(NSwitch_ne.getDeviceName());
                        if (NSwitch_ne.getDeviceTypeId() != null) {
                            DeviceType deviceType = this.deviceTypeService.selectObjById(NSwitch_ne.getDeviceTypeId());
                            DE.setRemoteDevicTypeeUuid(deviceType.getUuid());
                            DE.setRemoteDeviceType(deviceType.getName());
                        }
                    }
                }
            }
        }
        return ResponseUtil.ok(deMacList);
    }

    public List<Mac> getMacTagDe(String remoteDevice, String remoteIp){
        List<Mac> remoteDeviceList = new ArrayList<>();
        Map params = new HashMap();
        if(remoteIp != null && !"".equals(remoteIp)){
            params.clear();
            params.put("deviceIp", remoteIp);
            remoteDeviceList = this.macService.selectObjByMap(params);
        }else if(remoteDevice != null && !"".equals(remoteDevice)){
            params.clear();
            params.put("hostname", remoteDevice);
            remoteDeviceList = this.macService.selectObjByMap(params);
        }
        return remoteDeviceList;
    }

//    @RequestMapping("de")
    public Result mac_de1(){
        List<Mac> deMacList = this.macService.selectTagByDE();

        if(deMacList.size() > 0){
            Map<String, Mac> map = new HashMap<>();
            for (Mac mac : deMacList) {

                String remoteDevice = mac.getHostname() + mac.getRemoteDevice();
                String hostName = mac.getRemoteDevice() + mac.getHostname();
                if(map.get(remoteDevice) == null && map.get(hostName) == null){
                    map.put(remoteDevice, mac);
                }
            }
            deMacList.clear();
            for (Map.Entry<String, Mac> stringMacEntry : map.entrySet()) {
                deMacList.add(stringMacEntry.getValue());
            }
        }

        Map params = new HashMap();
        if(deMacList.size() > 0){
            for (Mac DE : deMacList) {
                params.clear();
                if(StringUtil.isNotEmpty(DE.getDeviceIp())){
                    params.put("ip", DE.getDeviceIp());
                    List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
                    if(networkElements.size() > 0){
                        NetworkElement networkElement = networkElements.get(0);
                        // 设置设备Uuid
                        DE.setDeviceUuid(networkElement.getUuid());
                        if(networkElement.getDeviceTypeId() != null){
                            DeviceType deviceType = this.deviceTypeService.selectObjById(networkElement.getDeviceTypeId());
                            DE.setDeviceTypeUuid(deviceType.getUuid());
                            DE.setDeviceType(deviceType.getName());
                        }
                        if(StringUtil.isNotEmpty(DE.getRemoteDevice())){
                            params.clear();
                            params.put("hostname", DE.getRemoteDevice());
                            List<Mac> remoteDeviceList = this.macService.selectObjByMap(params);
                            if(remoteDeviceList.size() > 0){
                                Mac remoteDevice = remoteDeviceList.get(0);
                                DE.setRemoteDeviceIp(remoteDevice.getDeviceIp());
                                DE.setRemoteDeviceName(remoteDevice.getDeviceName());
                                params.clear();
                                params.put("hostname", DE.getRemoteDevice());
                                params.put("remoteDevice", DE.getHostname());
                                List<Mac> portMac = this.macService.selectObjByMap(params);
                                if(portMac.size() > 0){
                                    DE.setPort(portMac.get(0).getRemotePort());
                                }
                                if(StringUtil.isNotEmpty(remoteDevice.getDeviceIp())){
                                    params.clear();
                                    params.put("ip", remoteDevice.getDeviceIp());
                                    List<NetworkElement> remote_networkElements = this.networkElementService.selectObjByMap(params);
                                    if(remote_networkElements.size() > 0) {
                                        NetworkElement remote_networkElement = remote_networkElements.get(0);
                                        DE.setRemoteDeviceUuid(remote_networkElement.getUuid());
                                        if (remote_networkElement.getDeviceTypeId() != null) {
                                            DeviceType deviceType = this.deviceTypeService.selectObjById(remote_networkElement.getDeviceTypeId());
                                            DE.setRemoteDevicTypeeUuid(deviceType.getUuid());
                                            DE.setRemoteDeviceType(deviceType.getName());
                                        }
                                    }
                                }

                            }


                        }
                    }
                }
                if(StringUtil.isNotEmpty(DE.getRemoteDevice())){
                    params.clear();
                    params.put("deviceName", DE.getDeviceName());
                    params.put("deleteStatus", 1);
                    List<NetworkElement> NSwitch_nes2 = this.networkElementService.selectObjByMap(params);
                    if(NSwitch_nes2.size() > 0){
                        NetworkElement NSwitch_ne = NSwitch_nes2.get(0);
                        DE.setDeviceUuid(NSwitch_ne.getUuid());
                        if (NSwitch_ne.getDeviceTypeId() != null) {
                            DeviceType deviceType = this.deviceTypeService.selectObjById(NSwitch_ne.getDeviceTypeId());
                            DE.setDeviceTypeUuid(deviceType.getUuid());
                            DE.setDeviceType(deviceType.getName());
                        }
                    }
                }
                if(StringUtil.isNotEmpty(DE.getRemoteDevice())){
                    params.clear();
                    params.put("deviceName", DE.getRemoteDevice());
                    params.put("deleteStatusList", Arrays.asList(0, 1));
                    List<NetworkElement> NSwitch_nes = this.networkElementService.selectObjByMap(params);
                    if(NSwitch_nes.size() > 0){
                        NetworkElement NSwitch_ne = NSwitch_nes.get(0);
                        DE.setRemoteDeviceUuid(NSwitch_ne.getUuid());
                        DE.setRemoteDeviceIp(NSwitch_ne.getIp());
                        DE.setRemoteDeviceName(NSwitch_ne.getDeviceName());
                        if (NSwitch_ne.getDeviceTypeId() != null) {
                            DeviceType deviceType = this.deviceTypeService.selectObjById(NSwitch_ne.getDeviceTypeId());
                            DE.setRemoteDevicTypeeUuid(deviceType.getUuid());
                            DE.setRemoteDeviceType(deviceType.getName());
                        }
                    }
                }
            }
        }
        return ResponseUtil.ok(deMacList);
    }


    @RequestMapping("dt")
    public Result macDT(@RequestBody MacDTO dto){
        if(dto == null){
            dto = new MacDTO();
        }
        Page<Mac> page = this.macService.selectDTAndDynamicByConditionQuery(dto);
        return ResponseUtil.ok(new PageInfo<Mac>(page));
    }

}
