package com.metoo.nrsm.core.config.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-22 14:42
 */
public class RedisDemo {

//    public static void main(String[] args) {
//        // 远程Redis服务器的IP地址和端口
//        String redisHost = "192.168.5.205"; // 替换为你的Redis服务器地址
//        int redisPort = 6379; // Redis默认端口
//
//        // 创建Jedis对象
//        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
//            // 如果Redis服务器需要密码，请设置密码
//             jedis.auth("metoo89745000");
//
//            // 检查连接是否成功
//            String pingResponse = jedis.ping();
//            System.out.println("连接成功: " + pingResponse);
//
//            // 写入数据到Redis
////            jedis.set("myKey", "Hello, Redis!");
//
//            // 从Redis读取数据
////            String value = jedis.get("<DNSLabel:'34-courier.push.apple.com'>");
//
//            System.out.println("从Redis读取的数据: " + jedis.hgetAll("<DNSLabel(6364)"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



    private static final int MAX_RETRIES = 3;
    private static final String REDIS_URI = "redis://127.0.0.1:6379";

    public static void main(String[] args) {
        RedisClient redisClient = RedisClient.create(REDIS_URI);
        StatefulRedisConnection<String, String> connection = null;

        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                connection = redisClient.connect();
                RedisCommands<String, String> syncCommands = connection.sync();
                System.out.println("Connected to Redis successfully.");
                // 执行Redis操作
                List<String> keys = syncCommands.keys("*");
                System.out.println("Keys: " + keys);
                break; // 连接成功则退出重试循环
            } catch (RedisConnectionException e) {
                System.err.println("Failed to connect to Redis, retrying... (" + (i + 1) + "/" + MAX_RETRIES + ")");
                if (i == MAX_RETRIES - 1) {
                    System.err.println("Max retries reached. Could not connect to Redis.");
                    // 这里可以记录日志或返回自定义的错误
                }
                try {
                    Thread.sleep(2000); // 重试前等待 2 秒
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (connection != null) {
            connection.close();
        }
        redisClient.shutdown();
    }


}
