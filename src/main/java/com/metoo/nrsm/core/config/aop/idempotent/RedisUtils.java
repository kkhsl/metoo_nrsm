package com.metoo.nrsm.core.config.aop.idempotent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 16:10
 *
 * redis工具类
 *
 */
@Slf4j
@Component
public class RedisUtils {

    /**
     * 默认RedisObjectSerializer序列化
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 加分布式锁
     */
    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 释放锁
     */
    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            //将参数key转为集合
            redisTemplate.delete(Arrays.asList(keys));
        }
    }
}
