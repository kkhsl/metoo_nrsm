package com.metoo.nrsm.core.config.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-22 15:46
 */
public class RedisScanDemo {

    public static void main(String[] args) {
        // 远程Redis服务器的IP地址和端口
        String redisHost = "localhost"; // 替换为你的Redis服务器地址
        int redisPort = 6379; // Redis默认端口

        // 创建Jedis对象
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            // 如果Redis服务器需要密码，请设置密码
             jedis.auth("123456");

            // 检查连接是否成功
            String pingResponse = jedis.ping();
            System.out.println("连接成功: " + pingResponse);

            // 获取全部键和值
            Map<String, String> allData = new HashMap<>();
            String cursor = "0";
            ScanParams scanParams = new ScanParams().match("*").count(100); // 每次扫描100个键

            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = String.valueOf(scanResult.getCursor());
                for (String key : scanResult.getResult()) {
                    String value = jedis.get(key);
                    allData.put(key, value);
                }
            } while (!cursor.equals("0"));

            // 输出所有键值对
            allData.forEach((key, value) -> System.out.println(key + " : " + value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test(){
        // 连接到Redis服务器
        Jedis jedis = new Jedis("192.168.5.205", 6379);

        // 如果Redis设置了密码，使用下面的代码进行认证
         jedis.auth("metoo89745000");

        // 选择数据库，db0的索引是0
        jedis.select(0);

        // 获取所有键
        Set<String> keys = jedis.keys("*");

        // 遍历所有键，获取对应的值
        for (String key : keys) {
            if (jedis.type(key).equals("list")) {
                List<String> listValues = jedis.lrange(key, 0, -1);
                System.out.println(listValues);
            }
        }

        // 关闭连接
        jedis.close();
    }


    @Test
    public void linux(){
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
            ScanParams scanParams = new ScanParams().count(3); // 每次扫描100个键

            List listAll = new ArrayList();
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = String.valueOf(scanResult.getCursor());
                for (String key : scanResult.getResult()) {
//                    String value = jedis.get(key);
//                    namespaceData.put(key, value);
                    if (jedis.type(key).equals("list")) {
                        List<String> listValues = jedis.lrange(key, 0, -1);
//                        System.out.println(listValues);
                        listAll.addAll(listValues);
                    }
                }
            } while (!cursor.equals("0"));

            // 指定输出文件的路径
            String filePath = "C:\\Users\\Administrator\\Desktop\\list\\output.txt";

            // 将列表写入文件
            writeListToFile(listAll, filePath);

            namespaceData.forEach((key, value) -> System.out.println(key + " : " + value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeListToFile(List<String> list, String filePath) {
        // 使用try-with-resources语句确保BufferedWriter在完成后正确关闭
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 遍历列表并将每个元素写入文件
            for (String line : list) {
                writer.write(line);
                writer.newLine(); // 写入行分隔符
            }
            System.out.println("列表已成功写入文件: " + filePath);
        } catch (IOException e) {
            System.err.println("写入文件时发生错误: " + e.getMessage());
        }
    }


    @Test
    public void traverseNestedListTest(){
        String arrayStr = "[[[\"newsofire.n.shifen.com.\", \"14.215.183.141\"]], [[\"newsofire.n.shifen.com.\", \"240e:e9:6002:93:0:ff:b019:33c9\"]]]";
        List<Object> nestedList = Arrays.asList(arrayStr);


        traverseNestedList(nestedList,  0);
    }

    public static void traverseNestedList(List<Object> list, int level) {
        for (Object element : list) {
            if (element instanceof List) {
                // 如果元素是一个List，递归调用
                traverseNestedList((List<Object>) element, level + 1);
            } else {
                // 打印当前层级和元素
                printElement(element, level);
            }
        }
    }

    public static void printElement(Object element, int level) {
        // 打印层级和元素
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
        System.out.println(element);
    }






    @Test
    public void linux2(){
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
            ScanParams scanParams = new ScanParams().count(3); // 每次扫描100个键

            List listAll = new ArrayList();
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = String.valueOf(scanResult.getCursor());
                for (String key : scanResult.getResult()) {
//                    String value = jedis.get(key);
//                    namespaceData.put(key, value);
                    if (jedis.type(key).equals("list")) {
                        List<String> listValues = jedis.lrange(key, 0, -1);
//                        System.out.println(listValues);
                        listAll.addAll(listValues);
                    }
                }
            } while (!cursor.equals("0"));

            genericList(listAll);
            namespaceData.forEach((key, value) -> System.out.println(key + " : " + value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void genericList(List<String> listStr){
        if(listStr.size() > 0){
            List domains = new ArrayList();
            Map<String, List> map = new HashMap();
            for (String data : listStr) {
                String[][] parse2 = JSON.parseObject(data, String[][].class);
                List<List<String>> towDimensional = JSON.parseObject(JSON.toJSONString(parse2),
                        new TypeReference<List<List<String>>>() {});
                if(towDimensional.size() > 0){
                    for (List<String> tow : towDimensional) {
                        String domain = tow.get(0);
                        String ip = tow.get(1);

                        List<List<String>> list = map.get(domain);

                        List<String> ipv4List = null;
                        List<String> ipv6List = null;
                        if(list == null){
                            list = new ArrayList();
                            ipv4List = new ArrayList();
                            ipv6List = new ArrayList();
                            list.add(ipv4List);
                            list.add(ipv6List);
                            map.put(domain, list);
                        }else{
                            ipv4List = list.get(0);
                            ipv6List = list.get(1);
                        }
                        if(Ipv6Util.verifyIpv6(ip)){
                            ipv6List.add(ip);
                        }else if (Ipv4Util.verifyIp(ip)){
                            ipv4List.add(ip);
                        }
                    }
                }
            }


            System.out.println(map);
        }
    }


}




