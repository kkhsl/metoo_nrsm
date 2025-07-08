package com.metoo.nrsm.core.manager.ap.utils;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.dto.ParamsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 16:04
 */
@Component
public class GecossApiUtil {

    public static String BASE_URL;
    public static final String PASSWORD = "admin";// 默认密码

    public static GecoosApi GECOOSAPI = null;// ZabbixApi实例


    @Value("${ac.api.url}")
    public void setUrl(String url) {
        GecossApiUtil.BASE_URL = url;
    }


    @PostConstruct
    public void init() {
        GecoosApi gecoosApi = new DefaultGecoosApi(BASE_URL);
        gecoosApi.init();
        try {
            gecoosApi.login(GecossApiUtil.PASSWORD);
            GECOOSAPI = gecoosApi;
        } catch (Exception e) {
            e.printStackTrace();
            GECOOSAPI = null;
        }
    }

    public static void verify() {
        if (GECOOSAPI == null) {
            GecoosApi gecoosApi = new DefaultGecoosApi(BASE_URL);
            gecoosApi.init();
            try {
                gecoosApi.login(GecossApiUtil.PASSWORD);
                GECOOSAPI = gecoosApi;
            } catch (Exception e) {
                e.printStackTrace();
                GECOOSAPI = null;
            }
        }
    }


    public static void login() {
        if (GECOOSAPI == null) {
            GecoosApi gecoosApi = new DefaultGecoosApi(BASE_URL);
            gecoosApi.init();
            try {
                gecoosApi.login(GecossApiUtil.PASSWORD);
                GECOOSAPI = gecoosApi;
            } catch (Exception e) {
                e.printStackTrace();
                GECOOSAPI = null;
            }
        }
    }

    public static JSONObject call(RequestParams request) {
        verify();
        if (GECOOSAPI != null) {
            JSONObject resJson = null;
            try {
                resJson = GECOOSAPI.call(request);
                if (resJson.getInteger("ret") == -99) {
                    GECOOSAPI = null;
                    login();
                    resJson = GECOOSAPI.call(request);
                }
                return resJson;
            } catch (Exception e) {
                e.printStackTrace();
                return new JSONObject();
            }
        }
        return new JSONObject();
    }

    public static JSONObject getCall(RequestParams request) {
        verify();
        if (GECOOSAPI != null) {
            JSONObject resJson = null;
            try {
                resJson = GECOOSAPI.getCall(request);
                if (resJson.getInteger("ret") == -99) {
                    GECOOSAPI = null;
                    login();
                    resJson = GECOOSAPI.getCall(request);
                }
                return resJson;
            } catch (Exception e) {
                e.printStackTrace();
                return new JSONObject();
            }
        }
        return new JSONObject();
    }

    public static RequestParams parseParam(ParamsDTO dto, String url) {
        RequestBuilder requestBuilder = RequestBuilder.newBuilder().uri(BASE_URL + url);
        if (dto != null) {
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(dto));
            for (Map.Entry<String, Object> entry : json.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    requestBuilder.paramEntry(key, value);
                }
            }
        }
        return requestBuilder.build();
    }

}
