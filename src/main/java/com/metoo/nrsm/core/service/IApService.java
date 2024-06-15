package com.metoo.nrsm.core.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-04 10:10
 */
public interface IApService {

    List<JSONObject> getOnlineAp();
}
