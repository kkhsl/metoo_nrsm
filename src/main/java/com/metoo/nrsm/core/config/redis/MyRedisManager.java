package com.metoo.nrsm.core.config.redis;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;

public class MyRedisManager<k,v> {

    private String cacheName;

    public String getCacheName(){
        return this.cacheName;
    }

    public MyRedisManager(String cacheName) {
        this.cacheName = cacheName;
    }


    public v get(k k) throws CacheException {
        System.out.println("get key : " + k);

        // 第一种
        //return (v) getRedisTemplate().opsForValue().get(k.toString());

        // 第二种
        return (v) getRedisTemplate().opsForHash().get(this.cacheName, k.toString());
    }

    public v put(k k, v v) throws CacheException {
        System.out.println("put key : " + k);
        System.out.println("put v " + v);
        // 第一种
        //getRedisTemplate().opsForValue().set(k.toString(), v);
        // 第二种
        getRedisTemplate().opsForHash().put(this.cacheName, k.toString(), v);
        return null;
    }

    public v remove(k k) throws CacheException {

        return (v) getRedisTemplate().opsForHash().delete(this.cacheName, k.toString());
    }

//    public v rename(k k) throws CacheException {
//
//        return (v) getRedisTemplate().opsForHash().
//    }


    public void clear() throws CacheException {
        getRedisTemplate().delete(this.cacheName);
    }

    public int size() {
        return getRedisTemplate().opsForHash().size(this.cacheName).intValue();
    }


    public Set<k> keys() {
        return getRedisTemplate().opsForHash().keys(this.cacheName);
    }

    public Collection<v> values() {
        return getRedisTemplate().opsForHash().values(this.cacheName);
    }


    private RedisTemplate getRedisTemplate(){
        return (RedisTemplate) ApplicationContextUtils.getBean("redisTemplate");
    }
}
