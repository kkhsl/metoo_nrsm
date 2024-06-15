package com.metoo.nrsm.core.manager.ap.utils;

import com.alibaba.fastjson.JSONObject;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 14:38
 */
public interface GecoosApi {

    void init();

    void destroy();

    String apiVersion();

    JSONObject call(RequestParams requestParams);

    JSONObject getCall(RequestParams requestParams);

    boolean login(String password);
}
