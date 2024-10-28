package com.metoo.nrsm.core.wsapi.utils;

import com.metoo.nrsm.core.config.redis.manager.MyRedisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RedisResponseUtils {

    @Autowired
    private static MyRedisManager redisWss = new MyRedisManager("ws");

    // 0:未变化 1：有变化
    @Async
    public void syncRedis(String sessionid, Object result, Integer type){
        if (sessionid == null || sessionid.isEmpty()) {
            return;  // 早期返回以减少嵌套
        }
        String key = sessionid + ":" + type;
        String key0 = key + ":0";
        Object value = redisWss.get(key0);
        String key1 = "";

        boolean k0flag = false;
        boolean k1flag = false;

        if(value == null || "".equals(value)){
            key1 = key + ":1";
            value = redisWss.get(key1);
            if(value != null && !value.equals("")){
                k1flag = true;
            }
        }else{
            k0flag = true;
        }
        if(value == null || "".equals(value)){
            redisWss.put(key + ":1", result);
        }else{
            boolean flag = Md5Crypt.getDiffrent(value, result);
            if(flag){
                if(k1flag){
                    redisWss.remove(key1);
                    redisWss.put(key + ":0", result);
                }
//                    if(k0flag){
//                        redisWss.remove(key0);
//                    }
            }else{
                if(k0flag){
                    redisWss.remove(key0);
                }
                if(k1flag){
                    redisWss.remove(key1);
                }
                redisWss.put(key + ":1", result);
            }
        }
    }


//    @Async 改为同步更新
    public void syncStrRedis(String sessionid, String result, Integer type){
        if(sessionid != null && !sessionid.equals("")){
            String key = sessionid + ":" + type;
            String key0 = key + ":0";
            Object value = redisWss.get(key0);
            String key1 = "";

            boolean k0flag = false;
            boolean k1flag = false;

            if(value == null || "".equals(value)){
                key1 = key + ":1";
                value = redisWss.get(key1);
                if(value != null && !value.equals("")){
                    k1flag = true;
                }
            }else{
                k0flag = true;
            }

            if(value == null || "".equals(value)){
                redisWss.put(key + ":1", result);
            }else{
                boolean flag = Md5Crypt.getDiffrent(value, result);
                if(flag){
                    if(k1flag){
                        redisWss.remove(key1);
                        redisWss.put(key + ":0", result);
                    }
//                    if(k0flag){
//                        redisWss.remove(key0);
//                    }
                }else{
                    if(k0flag){
                        redisWss.remove(key0);
                    }
                    if(k1flag){
                        redisWss.remove(key1);
                    }
                    redisWss.put(key + ":1", result);
                }
            }
        }
    }


    @Async
    public void syncStrRedis2(String sessionid, String result, Integer type){
        if(sessionid != null && !sessionid.equals("")){

            String key = sessionid + ":" + type;

            String key_0 = key + ":0";
            Object key_0_value = redisWss.get(key_0);

            String key_1 = key + ":1";

            boolean key_0_flag = false;
            boolean key_1_flag = false;
            if(key_0_value == null || "".equals(key_0_value)){
                key_0_value = redisWss.get(key_1);
                if(key_0_value != null && !key_0_value.equals("")){
                    key_1_flag = true;
                }
            }else{
                key_0_flag = true;
            }
            // 为null表示没数据，则为key_1设置数据，主动更新
            if(key_0_value == null || "".equals(key_0_value)){
                redisWss.put(key_1, result);
            }else{
                boolean flag = Md5Crypt.getDiffrent(key_0_value, result);
                if(flag){
                    if(key_1_flag){
                        redisWss.remove(key_1);
                        redisWss.put(key_0, result);
                    }
                }else{
                    if(key_0_flag){
                        redisWss.remove(key_0);
                    }
                    if(key_1_flag){
                        redisWss.remove(key_1);
                    }
                    redisWss.put(key + ":1", result);
                }
            }
        }
    }

    public static void rem(String sessionid, Integer type){

        String key0 = sessionid + ":" + type + ":0";

        Object value = redisWss.get(key0);

        if(value != null){
            redisWss.remove(key0);
        }else {
            String key1 = sessionid + ":" + type + ":1";

            value = redisWss.get(key1);

            if (value != null) {
                redisWss.remove(key1);
            }
        }
    }
}
