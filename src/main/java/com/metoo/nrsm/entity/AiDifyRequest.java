package com.metoo.nrsm.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AiDifyRequest {
    private String response_mode = "streaming";
    private Map<String, Object> inputs;
    private String user = "abc-123";

    /**
     * 添加数据结构方法
     */
    public void addData(String key, Object value) {
        if (null == this.inputs) {
            this.inputs = new HashMap<>(16);
        }
        this.inputs.put(key, value);
    }

}