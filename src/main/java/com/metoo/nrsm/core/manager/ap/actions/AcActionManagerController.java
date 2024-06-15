package com.metoo.nrsm.core.manager.ap.actions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.ap.utils.GecossApiUtil;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.ITerminalCountService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.ac.AcAction;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.ac.AcUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 16:48
 */
@RequestMapping("/admin/ac")
@RestController
public class AcActionManagerController {


    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private ITerminalService terminalService;

    @PostMapping("apsearch")
    public JSONObject apsearch(@RequestBody AcAction instance){
        JSONObject jsonObject = GecossApiUtil.getCall(GecossApiUtil.parseParam(instance, "apsearch"));
        return jsonObject;
    }

    @PostMapping("stasearch")
    public JSONObject stasearch(@RequestBody AcUser instance){
        JSONObject result = GecossApiUtil.getCall(GecossApiUtil.parseParam(instance, "stasearch"));
        JSONObject jsonObject = result;
        if(StringUtils.isNotBlank(String.valueOf(jsonObject.get("stalist")))/*jsonObject.get("stalist") != null && Strings.isNotBlank(String.valueOf(jsonObject.get("stalist")))*/){
            JSONArray jsonArray = jsonObject.getJSONArray("stalist");
            if(jsonArray != null){
                Map params = new HashMap();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    if(Strings.isNotBlank(obj.getString("mac"))){
                        params.clear();
                        params.put("mac", obj.getString("mac"));
                        List<Terminal> terminals = terminalService.selectObjByMap(params);
                        if(terminals.size() > 0){
                            Terminal terminal = terminals.get(0);
                            obj.put("ipv4", terminal.getV4ip());
                            obj.put("ipv41", terminal.getV4ip1());
                            obj.put("ipv42", terminal.getV4ip2());
                            obj.put("ipv43", terminal.getV4ip3());
                            obj.put("ipv6", terminal.getV6ip());
                            obj.put("ipv61", terminal.getV6ip1());
                            obj.put("ipv62", terminal.getV6ip2());
                            obj.put("ipv63", terminal.getV6ip3());
                        }
                    }
                }
            }
        }
        return jsonObject;
    }

    @GetMapping("sync")
    public Result sync(){
        AcAction instance = new AcAction();
        instance.setNumperpage(1000000);
        instance.setPagenum(1);
        JSONObject jsonObject = GecossApiUtil.getCall(GecossApiUtil.parseParam(instance, "apsearch"));
        if(Strings.isNotBlank(String.valueOf(jsonObject.get("aplist")))){
            JSONArray jsonArray = JSONObject.parseArray(String.valueOf(jsonObject.get("aplist")));
            Set<String> set = new HashSet();
            if(jsonArray != null){
                Map params = new HashMap();
                DeviceType deviceType = deviceTypeService.selectObjByType(8);
                for (Object ele : jsonArray) {
                    JSONObject obj = JSONObject.parseObject(String.valueOf(ele));
                    String name = obj.getString("name");
                    String ip = obj.getString("ip");
                    if(Strings.isNotBlank(name) && Strings.isNotBlank(ip)){
                        params.clear();
                        params.put("deviceName", name);
                        params.put("ip", ip);
                        List<NetworkElement> list = this.networkElementService.selectObjByMap(params);
                        if(list.size() <= 0){
                            NetworkElement ne = new NetworkElement();
                            ne.setDeviceName(name);
                            ne.setIp(ip);
                            ne.setType(3);
                            if(deviceType != null){
                                ne.setDeviceTypeId(deviceType.getId());
                            }
                            this.networkElementService.save(ne);
                        }
                        set.add(name);
                    }
                }
                params.clear();
                params.put("NotEqualdeviceNameSet", set);
                params.put("type", 3);
                List<NetworkElement> list = this.networkElementService.selectObjByMap(params);
                if(list.size() > 0){
                    for (NetworkElement networkElement : list) {
                        this.networkElementService.delete(networkElement.getId());
                    }
                }

            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }
}
