package com.metoo.nrsm.core.config.redis;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-22 15:48
 */
public class RedisNamespaceScanDemo {

    public static void main(String[] args) {
        // 远程Redis服务器的IP地址和端口
        String redisHost = "localhost"; // 替换为你的Redis服务器地址
        int redisPort = 6379; // Redis默认端口

        // 指定命名空间
        String namespace = "users";

        // 创建Jedis对象
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            // 如果Redis服务器需要密码，请设置密码
             jedis.auth("123456");

            // 检查连接是否成功
            String pingResponse = jedis.ping();
            System.out.println("连接成功: " + pingResponse);

            // 获取指定命名空间下的全部键和值
            Map<String, String> namespaceData = new HashMap<>();
            String cursor = "0";
            ScanParams scanParams = new ScanParams().match(namespace + ":*").count(100); // 每次扫描100个键

            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = String.valueOf(scanResult.getCursor());
                for (String key : scanResult.getResult()) {
                    String value = jedis.get(key);
                    namespaceData.put(key, value);
                }
            } while (!cursor.equals("0"));

            // 输出所有键值对
            namespaceData.forEach((key, value) -> System.out.println(key + " : " + value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void linux(){
        // 远程Redis服务器的IP地址和端口
        String redisHost = "192.168.5.205"; // 替换为你的Redis服务器地址
        int redisPort = 6379; // Redis默认端口

        // 指定命名空间
        String namespace = "<DNSLabel(6407)";

        // 创建Jedis对象
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            // 如果Redis服务器需要密码，请设置密码
            jedis.auth("metoo89745000");

            // 检查连接是否成功
            String pingResponse = jedis.ping();
            System.out.println("连接成功: " + pingResponse);

            // 获取指定命名空间下的全部键和值
            Map<String, String> namespaceData = new HashMap<>();
            String cursor = "0";
            ScanParams scanParams = new ScanParams().match(namespace + ":*").count(100); // 每次扫描100个键

            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = String.valueOf(scanResult.getCursor());
                for (String key : scanResult.getResult()) {
                    String value = jedis.get(key);
                    namespaceData.put(key, value);
                }
            } while (!cursor.equals("0"));

            // 输出所有键值对
            namespaceData.forEach((key, value) -> System.out.println(key + " : " + value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
