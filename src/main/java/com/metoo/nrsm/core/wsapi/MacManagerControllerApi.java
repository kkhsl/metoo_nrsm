package com.metoo.nrsm.core.wsapi;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.service.IApService;
import com.metoo.nrsm.core.service.IMacService;
import com.metoo.nrsm.core.wsapi.utils.NoticeWebsocketResp;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("ws/api/mac")
public class MacManagerControllerApi {

    @Autowired
    private IMacService macService;
    @Autowired
    private RedisResponseUtils redisResponseUtils;

    @ApiOperation("nswitch")
    @GetMapping(value = {"/nswitch"})
    public NoticeWebsocketResp nswitch(@RequestParam(value = "requestParams", required = false) String requestParams) {
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        if(!String.valueOf(requestParams).equals("")){
            Map map = JSONObject.parseObject(String.valueOf(requestParams), Map.class);
            String sessionId = (String)  map.get("sessionId");
            Map result = new HashMap();
            if(map.get("time") == null || StringUtil.isEmpty(String.valueOf(map.get("time")))){
                List<Mac> list = this.macService.selectTagDEWithNswitch();
                result.put("nswitchList", list);
            }else{
                // mac-nswitch历史数据
            }
            rep.setNoticeType("501");
            rep.setNoticeStatus(1);
            rep.setNoticeInfo(result);
            this.redisResponseUtils.syncStrRedis(sessionId, JSONObject.toJSONString(result, SerializerFeature.WriteMapNullValue), 501);
            return rep;
        }
        rep.setNoticeType("501");
        rep.setNoticeStatus(0);
        return rep;
    }
}
