package com.metoo.nrsm.core.wsapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.dto.NetworkElementDto;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.gather.snmp.utils.DeviceManager;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.wsapi.utils.NoticeWebsocketResp;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import com.metoo.nrsm.entity.NetworkElement;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-12 17:08
 */
@Slf4j
@RestController
@RequestMapping("/ws/api/ne")
public class NetworkManagerControllerApi {

    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private RedisResponseUtils redisResponseUtils;
    @Autowired
    private PythonExecUtils pythonExecUtils;
    @Autowired
    private DeviceManager deviceManager;

    /**
     *{"noticeType":"201","userId":"1","params":{"currentPage":1,"pageSize":20}}
     *
     * @param requestParams
     * @return
     */
//    @RequestMapping("/list")
//    public NoticeWebsocketResp testApi(@RequestParam(value = "requestParams") String requestParams){
//        NoticeWebsocketResp rep = new NoticeWebsocketResp();
//        if(requestParams != null && !requestParams.isEmpty()){
//            Map param = JSONObject.parseObject(requestParams, Map.class);
//            String sessionId = (String) param.get("sessionId");
//            Map result = new HashMap();
//            // 获取类型
//            NetworkElementDto dto = JSONObject.parseObject(param.get("params").toString(), NetworkElementDto.class);
//            if (dto == null) {
//                dto = new NetworkElementDto();
//            }
//            Page<NetworkElement> page = this.networkElementService.selectConditionQuery(dto);
//            if (page.getResult().size() > 0) {
//                for (NetworkElement ne : page.getResult()) {
//                    // snmp状态
//                    if(StringUtils.isEmpty(ne.getCommunity()) || StringUtils.isEmpty(ne.getVersion())){
//                        result.put(ne.getIp(), "3");
//                    }else{
//                        String path = Global.PYPATH + "gethostname.py";
//                        String[] params = {ne.getIp(), ne.getVersion(),
//                                ne.getCommunity()};
//                        String hostname = pythonExecUtils.exec(path, params);
//                        result.put(ne.getIp(), "2");
//                        if(StringUtils.isNotEmpty(hostname)){
//                            result.put(ne.getIp(), "1");
//                        }
//                    }
//                }
//                rep.setNoticeType("201");
//                rep.setNoticeStatus(1);
//                rep.setNoticeInfo(result);
//                this.redisResponseUtils.syncRedis(sessionId, result, 201);
//            }
//        }
//        rep.setNoticeStatus(0);
//        return rep;
//    }

    @RequestMapping("/list")
    public NoticeWebsocketResp testApi(@RequestParam(value = "requestParams") String requestParams){
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        if(requestParams != null && !requestParams.isEmpty()){
            Map param = JSONObject.parseObject(requestParams, Map.class);
            String sessionId = (String) param.get("sessionId");
            Map result = new HashMap();
            // 获取类型
            NetworkElementDto dto = JSONObject.parseObject(param.get("params").toString(), NetworkElementDto.class);
            if (dto == null) {
                dto = new NetworkElementDto();
            }
            Page<NetworkElement> page = this.networkElementService.selectConditionQuery(dto);
            if (page.getResult().size() > 0) {
                for (NetworkElement networkElement : page.getResult()) {
                    // snmp状态
                    if(StringUtils.isEmpty(networkElement.getCommunity()) || StringUtils.isEmpty(networkElement.getVersion())){
                        result.put(networkElement.getIp(), "3");
                    }else{
//                        String path = Global.PYPATH + "gethostname.py";
//                        String[] params = {networkElement.getIp(), networkElement.getVersion(),
//                                networkElement.getCommunity()};
//                        String hostname = pythonExecUtils.exec(path, params);
                        String hostname = deviceManager.getDeviceNameByIpAndCommunityVersion(networkElement);
                        result.put(networkElement.getIp(), "2");
                        if(StringUtils.isNotEmpty(hostname)){
                            result.put(networkElement.getIp(), "1");
                        }
                    }
                }
                rep.setNoticeType("201");
                rep.setNoticeStatus(1);
                rep.setNoticeInfo(result);
                this.redisResponseUtils.syncRedis(sessionId, result, 201);
            }
        }
        rep.setNoticeStatus(0);
        return rep;
    }


    /**
     *
     * {
     *  "noticeType":"202","userId":"1","time":"",
     *  "params":["192.168.5.102&e34c6576-f0f3-4dee-b287-e229bada9111",
     *                "192.168.5.212&ecf6ed9f-85aa-499b-9fb4-1296776c20a8",
     *               "192.168.5.191&7bfa8c56-a478-4fcf-8220-07eb9c9fb539",
     *               "192.168.5.192&896dcf46-2f19-4ba3-8976-cfe859f6231d",
     *               "192.168.5.193&4804a11d-6393-4287-9307-e4ee141e8ed1"]}
     *
     * @param requestParams
     * @return
     */
    @ApiOperation("拓扑|设备状态")
    @GetMapping("/snmp/status")
    public Object status(@RequestParam(value = "requestParams") String requestParams){
        List result = new ArrayList();
        String sessionId = "";
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        if(requestParams != null && !requestParams.equals("")){
            Map param = JSONObject.parseObject(String.valueOf(requestParams), Map.class);
            sessionId = (String) param.get("sessionId");
            List<Object> ips = JSONObject.parseObject(param.get("params").toString(), List.class);
            if(param.get("time") == null || StringUtil.isEmpty(String.valueOf(param.get("time")))){
                for (Object ip : ips){
                    try {
                        Map map = new HashMap();
                        String[] str = ip.toString().split("&");
                        // 获取端口snmp可用性
                        // snmp状态
                        Map params = new HashMap();
                        params.put("ip", str[0]);
                        List<NetworkElement> nes = this.networkElementService.selectObjByMap(params);
                        if(nes.size() > 0){
                            NetworkElement networkElement = nes.get(0);
                            // snmp状态
                            if(StringUtils.isEmpty(networkElement.getCommunity()) || StringUtils.isEmpty(networkElement.getVersion())){
                                map.put("snmp", "3");
                            }else{
//                                String path = Global.PYPATH + "gethostname.py";
//                                String[] args = {networkElement.getIp(), networkElement.getVersion(),
//                                        networkElement.getCommunity()};
//                                String hostname = pythonExecUtils.exec(path, args);
                                String hostname = deviceManager.getDeviceNameByIpAndCommunityVersion(networkElement);
                                map.put("snmp", "2");
                                if(StringUtils.isNotEmpty(hostname)){
                                    map.put("snmp", "1");
                                }
                            }
                            map.put("isIPv6", networkElement.isIsipv6());
                            map.put("uuid", str[1]);
                            result.add(map);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
            rep.setNoticeType("202");
            rep.setNoticeInfo(result);
            this.redisResponseUtils.syncStrRedis(sessionId, JSON.toJSONString(result), 202);
            return rep;
        }
        rep.setNoticeType("202");
        return rep;
    }


}
