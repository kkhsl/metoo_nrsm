package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.ap.utils.GecossApiUtil;
import com.metoo.nrsm.core.service.IApService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.ac.AcAction;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-04 10:11
 */
@Service
public class ApServiceImpl implements IApService {

    @Override
    public List<JSONObject> getOnlineAp() {
        List<JSONObject> list = new ArrayList<>();
        AcAction instance = new AcAction();
        instance.setNumperpage(1000000);
        instance.setPagenum(1);
        JSONObject jsonObject = GecossApiUtil.getCall(GecossApiUtil.parseParam(instance, "apsearch"));
        if(Strings.isNotBlank(String.valueOf(jsonObject.get("aplist")))) {
            JSONArray jsonArray = JSONObject.parseArray(String.valueOf(jsonObject.get("aplist")));
            if (jsonArray != null) {
                for (Object ele : jsonArray) {
                    JSONObject obj = JSONObject.parseObject(String.valueOf(ele));
                    if("yes".equalsIgnoreCase(obj.getString("online"))){
                        list.add(obj);
                    }
                }
            }
        }
        return list;
    }

}
