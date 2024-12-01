package com.metoo.nrsm.core.wsapi.utils;

import com.metoo.nrsm.core.config.redis.manager.MyRedisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SnmpStatusUtils {

    @Autowired
    private static MyRedisManager snmp = new MyRedisManager("snmp");

    // 优化redis数据获取，避免数据量过多，造成的性能问题

    // 获取在线设备，然后redis在线设备改为离线
    public void editSnmpStatus(List<String> keys){

        Set<String> hash_keys = snmp.keys();

        if(keys.size() <= 0){
            for (String hash_key : hash_keys) {
                snmp.put(hash_key, 0);
            }
            return;
        }

        // 离线设备，删除，避免冗余数据的增加
        for (String hash_key : hash_keys) {
            if(keys.contains(hash_key)){
                if(snmp.get(hash_key).equals(0)){
                    snmp.put(hash_key, 1);
                }/*else if(snmp.get(hash_key).equals(1)){
                    snmp.put(hash_key, 0);
                }*/
            }
        }
        keys.removeAll(hash_keys);
        if(keys.size() > 0){
            for (String key : keys) {
                snmp.put(key, 1);
            }
        }
    }

    // 频繁取，每分钟取，存
    public Set<String> getOnlineDevice(){
        Set<String> uuids = new HashSet<>();
        Set<String> hash_keys = snmp.keys();
        if(hash_keys.size() > 0){
            for (String hash_key : hash_keys) {
                Integer value = (Integer) snmp.get(hash_key);
                if(value == 1){
                    uuids.add(hash_key);
                };
            }
        }
        return uuids;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    // 分批读取
    public void scanValue() {
        Map<Object, Object> result = new HashMap<>();

        ScanOptions scanOptions = ScanOptions.scanOptions().count(2).build(); // 每次读取2个字段
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan("snmp", scanOptions);

        while (cursor.hasNext()) {
            Map.Entry<Object, Object> entry = cursor.next();
            result.put(entry.getKey(), entry.getValue());
        }
        System.out.println(result);


        List<String> keys = new ArrayList<>();
//        Cursor<byte[]> cursor2 = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions);
//        while (cursor.hasNext()) {
//            keys.add(new String(cursor2.next()));
//        }
//
        Cursor<Map.Entry<Object, Object>> cursor2 = redisTemplate.opsForHash().scan("snmp", scanOptions);
        while (cursor.hasNext()) {
            Map.Entry<Object, Object> entry = cursor2.next();
            keys.add((String) entry.getKey());
        }

        System.out.println(keys);

    }
}
