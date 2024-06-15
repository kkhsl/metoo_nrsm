package com.metoo.nrsm.core.config.redis;

import redis.clients.jedis.Jedis;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-22 14:42
 */
public class RedisDemo {

    public static void main(String[] args) {
        // 远程Redis服务器的IP地址和端口
        String redisHost = "192.168.5.205"; // 替换为你的Redis服务器地址
        int redisPort = 6379; // Redis默认端口

        // 创建Jedis对象
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            // 如果Redis服务器需要密码，请设置密码
             jedis.auth("metoo89745000");

            // 检查连接是否成功
            String pingResponse = jedis.ping();
            System.out.println("连接成功: " + pingResponse);

            // 写入数据到Redis
//            jedis.set("myKey", "Hello, Redis!");

            // 从Redis读取数据
//            String value = jedis.get("<DNSLabel:'34-courier.push.apple.com'>");

            System.out.println("从Redis读取的数据: " + jedis.hgetAll("<DNSLabel(6364)"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
