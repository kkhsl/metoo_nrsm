package com.metoo.nrsm.core.config.redis.manager;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.io.IOException;
import java.util.*;

@Slf4j
public class MyRedisManager<k,v> {

    private String cacheName;

    public String getCacheName(){
        return this.cacheName;
    }

    public MyRedisManager(String cacheName) {
        this.cacheName = cacheName;
    }


    public v get(k k) throws CacheException {
        try {
            return (v) getRedisTemplate().opsForHash().get(this.cacheName, k.toString());
        } catch (Exception e) {
            // 记录日志并抛出自定义的 CacheException
            throw new CacheException("Error getting value from Redis", e);
        }
    }

    public v put(k k, v v) throws CacheException {
        try {
            getRedisTemplate().opsForHash().put(this.cacheName, k.toString(), v);
            return v; // 返回插入的值而非 null
        } catch (Exception e) {
            throw new CacheException("Error putting value into Redis", e);
        }
    }

    public v remove(k k) throws CacheException {
        try {
            return (v) getRedisTemplate().opsForHash().delete(this.cacheName, k.toString());
        } catch (Exception e) {
            throw new CacheException("Error removing value from Redis", e);
        }
    }

    public void clear() throws CacheException {
        try {
            getRedisTemplate().delete(this.cacheName);
        } catch (Exception e) {
            throw new CacheException("Error clearing cache in Redis", e);
        }
    }

    public int size() {
        try {
            return getRedisTemplate().opsForHash().size(this.cacheName).intValue();
        } catch (Exception e) {
            // 日志记录并返回默认值
            return 0;
        }
    }


    public Set<k> keys() {
        try {
            return getRedisTemplate().opsForHash().keys(this.cacheName);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public Collection<v> values() {
        try {
            return getRedisTemplate().opsForHash().values(this.cacheName);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }


    // 分批读取
//    public void scanValue() {
//        Map<Object, Object> result = new HashMap<>();
//        ScanOptions scanOptions = ScanOptions.scanOptions().count(2).build(); // 每次读取2个字段
//        Cursor<Map.Entry<Object, Object>> cursor = getRedisTemplate().opsForHash().scan(this.cacheName, scanOptions);
//
//        List<String> keys = new ArrayList<>();
//        Cursor<byte[]> cursor2 = getRedisTemplate().getConnectionFactory().getConnection().scan(scanOptions);
//        while (cursor.hasNext()) {
//            keys.add(new String(cursor2.next()));
//        }
//
//        while (cursor.hasNext()) {
//            Map.Entry<Object, Object> entry = cursor.next();
//            result.put(entry.getKey(), entry.getValue());
//        }
//    }

    // 优化后的 scanValue 方法
    public void scanValue() {
        Map<Object, Object> result = new HashMap<>();
        ScanOptions scanOptions = ScanOptions.scanOptions().count(2).build(); // 每次读取2个字段
        try (Cursor<Map.Entry<Object, Object>> cursor = getRedisTemplate().opsForHash().scan(this.cacheName, scanOptions)) {
            while (cursor.hasNext()) {
                Map.Entry<Object, Object> entry = cursor.next();
                result.put(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            // 捕获并记录异常
            log.info("Error scanning values: {}" + e.getMessage());
        }
    }

    public Map<String, Integer> getAllWithValue2(int targetValue) {
        Map<String, Integer> result = new HashMap<>();
        ScanOptions scanOptions = ScanOptions.scanOptions().count(100).build(); // 每批100条
        try (Cursor<Map.Entry<Object, Object>> cursor = getRedisTemplate().opsForHash().scan(this.cacheName, scanOptions)) {
            while (cursor.hasNext()) {
                Map.Entry<Object, Object> entry = cursor.next();
                if (entry.getValue() instanceof Integer && (Integer) entry.getValue() == targetValue) {
                    result.put(entry.getKey().toString(), (Integer) entry.getValue());
                }
            }
        } catch (IOException e) {
            // 捕获并记录异常
            log.info("Error scanning for target value: {}" + e.getMessage());
        }
        return result;
    }


    // 性能较低
    public Map<String, Integer> getAllWithValue(int targetValue) {
        Map<String, Integer> result = new HashMap<>();
        try {
            Map<Object, Object> entries = getRedisTemplate().opsForHash().entries(this.cacheName);
            entries.forEach((k, v) -> {
                if (v instanceof Integer && (Integer) v == targetValue) {
                    result.put(k.toString(), (Integer) v);
                }
            });
        } catch (Exception e) {
            // 捕获并记录异常
            log.info("Error getting all values with target value: {}" + e.getMessage());
        }
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
