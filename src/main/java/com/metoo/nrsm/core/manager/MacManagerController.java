package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
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

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-22 11:52
 */

@RequestMapping("/admin/mac")
@RestController
public class MacManagerController {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Orange");

        list.removeAll(Collections.emptyList()); // 清空整个列表

        System.out.println(list); // 输出[]

        list.add("Apple");
        list.add("Banana");
        list.add("Orange");
        list.clear();
        list.removeAll(list); // 清空整个列表
    }


    @Autowired
    private IMacService macService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private IDhcpService dhcpService;
    @Autowired
    private IDhcp6Service dhcp6Service;

    // 拓扑信息
    // mac - DE 直连
    @RequestMapping("de")
    public Result mac_de(){
        Map params = new HashMap();
        params.put("tag", "DE");
        List<Mac> macList = this.macService.selectObjByMap(params);

        if(macList.size() > 0){
            Map<String, Mac> map = new HashMap<>();
            for (Mac mac : macList) {

                String key1 = mac.getHostname() + mac.getRemoteDevice();

                String key2 = mac.getRemoteDevice() + mac.getHostname();

                if(map.get(key1) == null && map.get(key2) == null){
                    map.put(key1, mac);
                }
            }
//            macList.removeAll(Collections.emptyList());
            macList.clear();
            for (Map.Entry<String, Mac> stringMacEntry : map.entrySet()) {
                macList.add(stringMacEntry.getValue());
            }
        }

        if(macList.size() > 0){
            for (Mac mac : macList) {
                params.clear();
                params.put("ip", mac.getDeviceIp());
                List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
                if(networkElements.size() > 0){
                    NetworkElement networkElement = networkElements.get(0);
                    mac.setDeviceUuid(networkElement.getUuid());
                    if(networkElement.getDeviceTypeId() != null){
                        DeviceType deviceType = this.deviceTypeService.selectObjById(networkElement.getDeviceTypeId());
                        mac.setDeviceTypeUuid(deviceType.getUuid());
                        mac.setDeviceType(deviceType.getName());
                    }
                    params.clear();
                    params.put("hostname", mac.getRemoteDevice());
                    List<Mac> remoteMacs = this.macService.selectObjByMap(params);
                    if(remoteMacs.size() > 0){
                        Mac remoteMac = remoteMacs.get(0);
                        mac.setRemoteDeviceIp(remoteMac.getDeviceIp());
                        mac.setRemoteDeviceName(remoteMac.getDeviceName());
                        params.clear();
                        params.put("hostname", mac.getRemoteDevice());
                        params.put("remoteDevice", mac.getHostname());
                        List<Mac> portMac = this.macService.selectObjByMap(params);
                        if(portMac.size() > 0){
                            mac.setPort(portMac.get(0).getRemotePort());
                        }
                        params.clear();
                        params.put("ip", remoteMac.getDeviceIp());
                        List<NetworkElement> remote_networkElements = this.networkElementService.selectObjByMap(params);
                        if(networkElements.size() > 0) {
                            NetworkElement remote_networkElement = remote_networkElements.get(0);
                            mac.setRemoteDeviceUuid(remote_networkElement.getUuid());
                            if (remote_networkElement.getDeviceTypeId() != null) {
                                DeviceType deviceType = this.deviceTypeService.selectObjById(remote_networkElement.getDeviceTypeId());
                                mac.setRemoteDevicTypeeUuid(deviceType.getUuid());
                                mac.setRemoteDeviceType(deviceType.getName());
                            }
                        }
                    }
                }
            }
        }
        return ResponseUtil.ok(macList);
    }

//    @RequestMapping("dt")
//    public Result macDT(){
//        Map params = new HashMap();
//        params.put("tag", "DT");
//        List<Mac> macs = this.macService.selectDTAndDynamicByMap(params);
////        if(macs.size() > 0){
////            Map params = new HashMap();
////            for (Mac mac : macs) {
////                mac.setMark("0");
////                params.clear();
////                params.put("lease_array", mac.getV4ipAll());
////                List<Dhcp> dhcps = this.dhcpService.selectObjByMap(params);
////                if(dhcps.size() > 0){
////                    mac.setMark("1");
////                }
////                params.clear();
////                params.put("iaaddr_array", mac.getV6ipAll());
////                List<Dhcp6> dhcp6s = this.dhcp6Service.selectObjByMap(params);
////                if(dhcp6s.size() > 0){
////                    mac.setMark("1");
////                }
////            }
////        }
//        return  ResponseUtil.ok(macs);
//    }


    @RequestMapping("dt")
    public Result macDT(@RequestBody MacDTO dto){
        if(dto == null){
            dto = new MacDTO();
        }
        Page<Mac> page = this.macService.selectDTAndDynamicByConditionQuery(dto);
        return ResponseUtil.ok(new PageInfo<Mac>(page));
    }

}
