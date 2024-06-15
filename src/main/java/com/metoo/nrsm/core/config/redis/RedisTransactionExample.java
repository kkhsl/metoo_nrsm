package com.metoo.nrsm.core.config.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 读取数据，并清空数据库（原子操作）
 * @author HKK
 * @version 1.0
 * @date 2024-05-23 15:58
 */
public class RedisTransactionExample {


    public static void main(String[] args) {
        // 连接到Redis服务器（默认端口为6379）
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            System.out.println("连接成功");

            // 如果Redis服务器有密码，可以使用如下方式连接：
             jedis.auth("123456");

            // 选择第0个数据库
            jedis.select(5);

            // Lua脚本

            // Lua脚本
            String script =
                    "local result = redis.call('KEYS', '*')\n" +
                            "local data = {}\n" +
                            "for i, key in ipairs(result) do\n" +
                            "    data[key] = redis.call('GET', key)\n" +
                            "end\n" +
                            "redis.call('FLUSHDB')\n" +
                            "return cjson.encode(data)";

            // 执行Lua脚本
            String result = (String) jedis.eval(script);

            // 解析返回的数据
            System.out.println("Data read from Redis and flushed: " + result);
        }
    }
}
