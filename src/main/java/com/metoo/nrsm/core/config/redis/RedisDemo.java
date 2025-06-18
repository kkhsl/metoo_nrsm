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
