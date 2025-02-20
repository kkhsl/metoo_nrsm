package com.metoo.nrsm.core.config.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-23 15:13
 */
public class MergeArraysExample {


    public static void main(String[] args) {
        // 示例数据
        String[][] data1 = {{"fa000000054.resources.office.net.", "23.56.233.67"}};
        String[][] data2 = {
                {"fa000000054.resources.office.net.", "2600:1417:4400:2b9::2c07"},
                {"fa000000054.resources.office.net.", "2600:1417:4400:28a::2c07"}
        };

        // 合并两个数组列表
        List<String[]> list = new ArrayList<>();
        list.addAll(Arrays.asList(data1));
        list.addAll(Arrays.asList(data2));

        // 聚合数据
        Map<String, Set<String>> aggregatedData = new HashMap<>();

        for (String[] pair : list) {
            String key = pair[0];
            String value = pair[1];

            aggregatedData.computeIfAbsent(key, k -> new HashSet<>()).add(value);
        }

        // 打印结果
        for (Map.Entry<String, Set<String>> entry : aggregatedData.entrySet()) {
            System.out.println("Key: " + entry.getKey());
            System.out.println("Values: " + entry.getValue());
        }
    }

    @Test
    public void linux2(){

        // 使用多线程处理数据
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Set<Future<List<String>>> futures = new HashSet<>();


        // 远程Redis服务器的IP地址和端口
        String redisHost = "192.168.5.205"; // 替换为你的Redis服务器地址
        int redisPort = 6379; // Redis默认端口

        // 指定命名空间
//        String namespace = "*";

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
            ScanParams scanParams = new ScanParams().count(100); // 每次扫描1000个键

            List listAll = new ArrayList();

            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = scanResult.getCursor();
                for (String key : scanResult.getResult()) {

                    Future<List<String>> future = executor.submit(() -> {
                        List dataList = new ArrayList();
                        if (jedis.type(key).equals("list")) {
                            List<String> listValues = jedis.lrange(key, 0, -1);
                            dataList.addAll(listValues);
                        }
                        return dataList;
                    });

                    listAll.addAll(future.get());
                }

            } while (!cursor.equals("0"));

            // 关闭线程池
            executor.shutdown();

            // 等待所有任务完成
            executor.awaitTermination(1, TimeUnit.MINUTES);

            for (Future<List<String>> future : futures) {
                List<String> result = future.get();
                listAll.addAll(result);
            }

            genericList(listAll);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void genericList(List<String> listStr){
        if(listStr.size() > 0){

            // 聚合数据
            Map<String, Map<String, Set<String>>> aggregatedData = new HashMap<>();

            for (String data : listStr) {
                String[][] parse2 = JSON.parseObject(data, String[][].class);
                List<List<String>> towDimensional = JSON.parseObject(JSON.toJSONString(parse2),
                        new TypeReference<List<List<String>>>() {});
                if(towDimensional.size() > 0){
                    for (List<String> tow : towDimensional) {
                        String domain = tow.get(0);
                        String ip = tow.get(1);
                        // 判断value是IPv4还是IPv6
                        String ipType;
                        if(Ipv6Util.verifyIpv6(ip)){
                            ipType = "IPv6";
                        }else if (Ipv4Util.verifyIp(ip)){
                            ipType = "IPv4";
                        }else {
                            ipType = "Unknown";
                        }


                        // 初始化嵌套Map
                        aggregatedData
                                .computeIfAbsent(domain, k -> new HashMap<>())
                                .computeIfAbsent(ipType, k -> new HashSet<>())
                                .add(ip);
                    }
                }
            }
            System.out.println(aggregatedData);
        }
    }



    @Test
    public void test_write() {
        // 连接到 Redis 服务
        Jedis jedis = new Jedis("localhost", 6379);

        try {
            jedis.auth("123456");

            // 清空现有数据（测试用，生产环境慎用）
            jedis.select(5);


            jedis.flushDB();


            // 示例数据，存入 Redis
            String[][] data = {
                    {"fa000000054.resources.office.net.", "23.56.233.67"},
                    {"fa000000054.resources.office.net.", "2600:1417:4400:2b9::2c07"},
                    {"fa000000054.resources.office.net.", "2600:1417:4400:28a::2c07"}
                    // 添加更多示例数据
            };

            // 存入 Redis
            for (String[] pair : data) {
                jedis.rpush(pair[0], pair[1]);
            }

        }finally {

        }
    }
}
