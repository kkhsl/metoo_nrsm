package com.metoo.nrsm.core.config.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-22 16:09
 */
public class RedisListNamespaceDemo {

    public static void main(String[] args) {
        // 远程Redis服务器的IP地址和端口
        String redisHost = "localhost"; // 替换为你的Redis服务器地址
        int redisPort = 6379; // Redis默认端口

        // 指定命名空间
        String namespace = "app:users";

        // 创建Jedis对象
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            // 如果Redis服务器需要密码，请设置密码
             jedis.auth("123456");

            // 检查连接是否成功
            String pingResponse = jedis.ping();
            System.out.println("连接成功: " + pingResponse);

            // 使用命名空间创建和操作List
            String listKey = namespace + ":user1:activities";

            // 将数据添加到List的尾部
            jedis.rpush(listKey, "login", "view_page", "logout");

            String listKey2 = namespace + ":user2:activities";
            jedis.rpush(listKey2, "login", "view_page", "logout");


            // 获取List中的所有元素
            List<String> activities = jedis.lrange(listKey, 0, -1);
            System.out.println("User1的活动记录: " + activities);

            // 删除并获取List中的第一个元素
            String firstActivity = jedis.lpop(listKey);
            System.out.println("删除的第一个活动: " + firstActivity);

            // 获取操作后List中的所有元素
            activities = jedis.lrange(listKey, 0, -1);
            System.out.println("操作后User1的活动记录: " + activities);

            // 获取指定命名空间下的所有List键和值
            Map<String, List<String>> namespaceData = new HashMap<>();
            String cursor = "0";
            ScanParams scanParams = new ScanParams().match(namespace + ":*").count(100); // 每次扫描100个键

            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = String.valueOf(scanResult.getCursor());
                for (String key : scanResult.getResult()) {
                    if (jedis.type(key).equals("list")) {
                        List<String> listValues = jedis.lrange(key, 0, -1);
                        namespaceData.put(key, listValues);
                    }
                }
            } while (!cursor.equals("0"));

            // 输出所有List键值对
            namespaceData.forEach((key, value) -> System.out.println(key + " : " + value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
