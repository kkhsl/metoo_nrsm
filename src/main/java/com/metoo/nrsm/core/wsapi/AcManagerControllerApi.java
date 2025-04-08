package com.metoo.nrsm.core.wsapi;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.service.IApService;
import com.metoo.nrsm.core.wsapi.utils.NoticeWebsocketResp;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-04 10:07
 */
@Slf4j
@RestController
@RequestMapping("ws/api/ac/ap")
public class AcManagerControllerApi {

    @Autowired
    private IApService apService;
    @Autowired
    private RedisResponseUtils redisResponseUtils;

    /**
     * {"noticeType":"401", "params":["一楼AP",  "三楼AP"],"userId":47}
     * @param requestParams
     * @return
     */
    @RequestMapping("/online")
    public NoticeWebsocketResp testApi(@RequestParam(value = "requestParams") String requestParams){
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        Map result = new HashMap();
        if(!String.valueOf(requestParams).equals("")){
            Map map = JSONObject.parseObject(String.valueOf(requestParams), Map.class);
            String sessionId = (String)  map.get("sessionId");
            List<String> names = JSONObject.parseObject(String.valueOf(map.get("params")), List.class);
            if(names != null && names.size() > 0){
                List<JSONObject> jsonObjects = this.apService.getOnlineAp();
                if(jsonObjects.size() > 0){
                    for (String name : names) {
                        result.put(name, false);
                        for (JSONObject jsonObject : jsonObjects) {
                            if(jsonObject.getString("name") != null
                                    && name.equalsIgnoreCase(jsonObject.getString("name"))){
                                result.put(name, true);
                                break;
                            }
                        }
                    }
                }
            }
            rep.setNoticeType("401");
            rep.setNoticeStatus(1);
            rep.setNoticeInfo(result);
            this.redisResponseUtils.syncStrRedis(sessionId, JSONObject.toJSONString(result), 401);
            return rep;
        }
        rep.setNoticeType("401");
        rep.setNoticeStatus(0);
        return rep;
    }
}
