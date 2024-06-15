package com.metoo.nrsm.core.manager.ap.utils;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 14:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestParams {

    private Map<String, Object> params = new HashMap();
    private String uri;

    public void putParam(String key, Object value) {
        this.params.put(key, value);
    }

    public Object removeParam(String key) {
        return this.params.remove(key);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
