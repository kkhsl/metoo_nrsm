package com.metoo.nrsm.core.wsapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.manager.utils.MacUtils;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.ITerminalCountService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.service.IVendorService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.wsapi.utils.NoticeWebsocketResp;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalCount;
import com.metoo.nrsm.entity.Vendor;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-11 11:10
 */
@Slf4j
@RestController
@RequestMapping("/ws/api/terminal")
public class TerminalManagerControllerApi {

    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private RedisResponseUtils redisResponseUtils;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private IVendorService vendorService;
    @Autowired
    private MacUtils macUtils;
    @Autowired
    private ITerminalCountService terminalCountService;

    @ApiOperation("设备 Mac (DT))")
    @GetMapping(value = {"/dt"})
    public NoticeWebsocketResp getObjMac(@RequestParam(value = "requestParams", required = false) String requestParams) {
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        if(!String.valueOf(requestParams).equals("")){
            Map map = JSONObject.parseObject(String.valueOf(requestParams), Map.class);
            String sessionId = (String)  map.get("sessionId");
            List<String> list = JSONObject.parseObject(String.valueOf(map.get("params")), List.class);
            Map result = new HashMap();
            Map params = new HashMap();
            if(map.get("time") == null || StringUtil.isEmpty(String.valueOf(map.get("time")))){
                for (String uuid : list) {
                    params.clear();
                    params.put("deviceUuid", uuid);
                    params.put("online", true);
                    List<Terminal> terminals = terminalService.selectObjByMap(params);
                    macUtils.terminalJoint(terminals);
                    for (Terminal terminal : terminals) {
                        DeviceType deviceType = deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                        if(deviceType != null){
                            terminal.setDeviceTypeName(deviceType.getName());
                            terminal.setDeviceTypeUuid(deviceType.getUuid());
                        }
                        if(terminal.getVendorId() != null && !terminal.getVendorId().equals("")){
                            Vendor vendor = this.vendorService.selectObjById(terminal.getVendorId());
                            if(vendor != null){
                                terminal.setVendorName(vendor.getName());
                            }
                        }
                    }
                    result.put(uuid, terminals);
                }
            }else{
                // mac历史数据；nrsm暂时没有记录
                for (String uuid : list) {
                    params.clear();
                    params.put("deviceUuid", uuid);
                    params.put("online", true);
                    params.put("time", map.get("time"));
                    List<Terminal> terminals = terminalService.selectObjHistoryByMap(params);
                    macUtils.terminalJoint(terminals);
                    for (Terminal terminal : terminals) {
                        DeviceType deviceType = deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                        if(deviceType != null){
                            terminal.setDeviceTypeName(deviceType.getName());
                            terminal.setDeviceTypeUuid(deviceType.getUuid());
                        }
                        if(terminal.getVendorId() != null && !terminal.getVendorId().equals("")){
                            Vendor vendor = this.vendorService.selectObjById(terminal.getVendorId());
                            if(vendor != null){
                                terminal.setVendorName(vendor.getName());
                            }
                        }
                    }
                    result.put(uuid, terminals);
                }
            }
            rep.setNoticeType("101");
            rep.setNoticeStatus(1);
            rep.setNoticeInfo(result);
            this.redisResponseUtils.syncStrRedis(sessionId, JSONObject.toJSONString(result), 101);
            return rep;
        }
        rep.setNoticeType("101");
        rep.setNoticeStatus(0);
        return rep;
    }

    /*
    {"noticeType":"102","userId":"1","time":"","params":{"dtGroupDragAddHNFwsjWNtxdRSkiKX7bFFhJaEQHKWa":["6e:2e:10:93:cb:18"],"dtGroupDragAddHNFwsjWNtxdRSkiKX7bFFhJaEQHKWa2":["00:50:79:66:68:56","00:50:79:66:68:57"],"dtGroupDragAddHNFwsjWNtxdRSkiKX7bFFhJaEQHKWa3":[],"dtGroupDragAddHNFwsjWNtxdRSkiKX7bFFhJaEQHKWa4":[]}}
     */
    @ApiOperation("102：终端")
    @GetMapping("/partition")
    public NoticeWebsocketResp partitionTerminal(@RequestParam(value = "requestParams", required = false) String requestParams){
        Map<String, Object> requestParam = JSONObject.parseObject(requestParams, Map.class);
                String sessionId = (String) requestParam.get("sessionId");
        Map<String, JSONArray> param = JSONObject.parseObject(String.valueOf(requestParam.get("params")), Map.class);
        Map result = new HashMap();
        if(!param.isEmpty()){
            Map params = new HashMap();
            if(param.get("time") == null || StringUtil.isEmpty(String.valueOf(param.get("time")))){
                Set<Map.Entry<String, JSONArray>> keys = param.entrySet();
                for (Map.Entry<String, JSONArray> entry : keys) {
                    if (entry.getValue() != null && entry.getValue().size() > 0) {
                        List list = new ArrayList<>();
                        entry.getValue().stream().forEach(m -> {
                            params.clear();
                            params.put("mac", m);
                            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
                            macUtils.terminalJoint(terminals);
                            if (terminals.size() > 0) {
                                Terminal terminal = terminals.get(0);
                                DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                                if (deviceType != null) {
                                    terminal.setDeviceTypeName(deviceType.getName());
                                    terminal.setDeviceTypeUuid(deviceType.getUuid());
                                }
                                list.add(terminal);
                            }
                        });
                        result.put(entry.getKey(), list);
                    }
                }
            }else{
                Set<Map.Entry<String, JSONArray>> keys = param.entrySet();
                for (Map.Entry<String, JSONArray> entry : keys) {
                    if (entry.getValue() != null && entry.getValue().size() > 0) {
                        List list = new ArrayList<>();
                        entry.getValue().stream().forEach(m -> {
                            params.clear();
                            params.put("mac", m);
                            params.put("time", param.get("time"));
                            List<Terminal> terminals = this.terminalService.selectObjHistoryByMap(params);
                            macUtils.terminalJoint(terminals);
                            if (terminals.size() > 0) {
                                Terminal terminal = terminals.get(0);
                                DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                                if (deviceType != null) {
                                    terminal.setDeviceTypeName(deviceType.getName());
                                    terminal.setDeviceTypeUuid(deviceType.getUuid());
                                }
                                list.add(terminal);
                            }
                        });
                        result.put(entry.getKey(), list);
                    }
                }
            }

            NoticeWebsocketResp rep = new NoticeWebsocketResp();
            rep.setNoticeType("102");
            rep.setNoticeStatus(1);
            rep.setNoticeInfo(result);
            this.redisResponseUtils.syncStrRedis(sessionId, JSON.toJSONString(result), 102);
            return rep;
        }
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        rep.setNoticeType("102");
        rep.setNoticeStatus(0);
        return rep;
    }

    @ApiOperation("103：终端")
    @GetMapping("/count")
    public NoticeWebsocketResp count(@RequestParam(value = "requestParams", required = false)
                                                 String requestParams){
        Map<String, Object> requestParam = JSONObject.parseObject(requestParams, Map.class);
        String sessionId = (String) requestParam.get("sessionId");
        Map params = new HashMap();
        TerminalCount terminalCount = null;
        if(requestParam.get("time") == null || StringUtil.isEmpty(String.valueOf(requestParam.get("time")))){
            params.clear();
            params.put("time", DateTools.gatherDate());
            terminalCount = this.terminalCountService.selectHistoryObjByMap(params);
        }else{
            params.clear();
            params.put("time", requestParam.get("time"));
            terminalCount = this.terminalCountService.selectHistoryObjByMap(params);
        }

        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        rep.setNoticeType("103");
        rep.setNoticeStatus(1);
        rep.setNoticeInfo(terminalCount);
        this.redisResponseUtils.syncStrRedis(sessionId, JSON.toJSONString(terminalCount), 103);
        return rep;
    }
}
