package com.metoo.nrsm.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AiRequest {
    private String type = "1";
    private Map<String, Object> data;
    private Map<String, String> params;

    // 构造方法
    public AiRequest(String ip) {
        this.params = new HashMap<>();
        this.params.put("ip", ip);
        this.data = new HashMap<>();
    }

    // 添加数据结构方法
    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

}