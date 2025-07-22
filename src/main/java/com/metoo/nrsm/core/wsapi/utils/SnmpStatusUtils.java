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

    public static void main(String[] args) {
        MyRedisManager snmp = new MyRedisManager("snmp");
        snmp.put("bfc0e2f4-dd8a-4f5e-886e-305319e22005", 0);
    }

    private static MyRedisManager snmp = new MyRedisManager("snmp");

    // 获取在线设备，然后redis在线设备改为离线
    public void editSnmpStatus(Set<String> newDevices) {

        Set<String> existingDevices = snmp.keys();

        // 如果没有新设备，全部设为 0
        if (newDevices.isEmpty()) {
            for (String hash_key : existingDevices) {
                snmp.put(hash_key, 0);
            }
            return;
        }

        // 离线设备处理：更新当前设备状态
        for (String device : existingDevices) {
            if (newDevices.contains(device)) {
                if (snmp.get(device).equals(0)) {
                    snmp.put(device, 1);
                }
            } else {
                // 如果设备不在新设备列表中，设置为离线状态（0）
                snmp.put(device, 0);
            }
        }

        // 对于新设备，若当前设备列表中没有，设为 1
        newDevices.removeAll(existingDevices);
        if (newDevices.size() > 0) {
            for (String key : newDevices) {
                snmp.put(key, 1);
            }
        }
    }

    // 获取在线设备UUID
//    public Set<String> getOnlineDevice() {
//        Set<String> uuIds = new HashSet<>();
//        Set<String> hash_keys = null;
//        try {
//            hash_keys = snmp.keys();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (hash_keys != null && !hash_keys.isEmpty()) {
//            for (String hash_key : hash_keys) {
//                Integer value = (Integer) snmp.get(hash_key);
//                if (value == 1) {
//                    uuIds.add(hash_key);
//                }
//            }
//        }
//        return uuIds;
//    }

    /**
     * 获取所有在线设备UUID集合
     *
     * @return 返回在线设备UUID集合（不会返回null）
     */
    public Set<String> getOnlineDevice() {
        // 直接获取所有值为1的在线设备
        Map<String, Integer> onlineDevices = snmp.getAllWithValue(1);

        // 如果结果为空则返回空集合，避免NPE
        if (onlineDevices == null || onlineDevices.isEmpty()) {
            return Collections.emptySet();
        }

        // 直接返回keySet（已经是Set<String>）
        return onlineDevices.keySet();
    }


    // 优化redis数据获取，避免数据量过多，造成的性能问题
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
    }
}
