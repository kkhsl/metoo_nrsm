package com.metoo.nrsm.core.config.redis.manager;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.io.IOException;
import java.util.*;

public class MyRedisManager<k,v> {

    private String cacheName;

    public String getCacheName(){
        return this.cacheName;
    }

    public MyRedisManager(String cacheName) {
        this.cacheName = cacheName;
    }


    public v get(k k) throws CacheException {

        // 第二种
        return (v) getRedisTemplate().opsForHash().get(this.cacheName, k.toString());
    }

    public v put(k k, v v) throws CacheException {
        // 第二种
        getRedisTemplate().opsForHash().put(this.cacheName, k.toString(), v);
        return null;
    }

    public v remove(k k) throws CacheException {

        return (v) getRedisTemplate().opsForHash().delete(this.cacheName, k.toString());
    }



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

    // 分批读取
    public void scanValue() {
        Map<Object, Object> result = new HashMap<>();
        ScanOptions scanOptions = ScanOptions.scanOptions().count(2).build(); // 每次读取2个字段
        Cursor<Map.Entry<Object, Object>> cursor = getRedisTemplate().opsForHash().scan(this.cacheName, scanOptions);

        List<String> keys = new ArrayList<>();
        Cursor<byte[]> cursor2 = getRedisTemplate().getConnectionFactory().getConnection().scan(scanOptions);
        while (cursor.hasNext()) {
            keys.add(new String(cursor2.next()));
        }

        while (cursor.hasNext()) {
            Map.Entry<Object, Object> entry = cursor.next();
            result.put(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Integer> getAllWithValue2(int targetValue) {
        Map<String, Integer> result = new HashMap<>();
        ScanOptions scanOptions = ScanOptions.scanOptions().count(100).build(); // 每批100条

        Cursor<Map.Entry<Object, Object>> cursor = getRedisTemplate()
                .opsForHash()
                .scan(this.cacheName, scanOptions);

        try {
            while (cursor.hasNext()) {
                Map.Entry<Object, Object> entry = cursor.next();
                if (entry.getValue() instanceof Integer &&
                        (Integer)entry.getValue() == targetValue) {
                    result.put(entry.getKey().toString(), (Integer)entry.getValue());
                }
            }
        } finally {
            try {
                cursor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }


    // 性能较低
    public Map<String, Integer> getAllWithValue(int targetValue) {
        Map<Object, Object> entries = getRedisTemplate()
                .opsForHash()
                .entries(this.cacheName);
        Map<String, Integer> result = new HashMap<>();
        entries.forEach((k, v) -> {
            if (v instanceof Integer && (Integer)v == targetValue) {
                result.put(k.toString(), (Integer)v);
            }
        });
        return result;
    }


//    public Map<String, Integer> getAllWithValue(int targetValue) {
//        String luaScript =
//                "local result = {} " +
//                        "local keys = redis.call('HKEYS', KEYS[1]) " +
//                        "for _, key in ipairs(keys) do " +
//                        "    local value = redis.call('HGET', KEYS[1], key) " +
//                        "    if tonumber(value) == ARGV[1] then " +
//                        "        table.insert(result, key) " +
//                        "        table.insert(result, value) " +
//                        "    end " +
//                        "end " +
//                        "return result";
//
//        List<Object> results = getRedisTemplate().execute(
//                new DefaultRedisScript<>(luaScript, List.class),
//                Collections.singletonList(this.cacheName),
//                String.valueOf(targetValue));
//
//        Map<String, Integer> resultMap = new HashMap<>();
//        for (int i = 0; i < results.size(); i += 2) {
//            resultMap.put(
//                    results.get(i).toString(),
//                    Integer.parseInt(results.get(i+1).toString()));
//        }
//
//        return resultMap;
//    }

    private RedisTemplate getRedisTemplate(){
        return (RedisTemplate) ApplicationContextUtils.getBean("redisTemplate");
    }
}
