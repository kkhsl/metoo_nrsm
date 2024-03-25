package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.DhcpDto;
import com.metoo.nrsm.core.service.IDhcpService;
import com.metoo.nrsm.core.utils.dhcp.DhcpUtils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Dhcp;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.Internet;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-15 17:06
 */
@RequestMapping("/admin/dhcp")
@RestController
public class DhcpManagerController {

    @Autowired
    private IDhcpService dhcpService;

    @PostMapping("/list")
    public Result list(@RequestBody DhcpDto dto){
        Page<Dhcp> page = this.dhcpService.selectConditionQuery(dto);
        if(page.getResult().size() > 0) {
            return ResponseUtil.ok(new PageInfo<Dhcp>(page));
        }
        return ResponseUtil.ok();
    }

    @GetMapping("getdhcp")
    public Result internet(){
       String result = this.dhcpService.getdhcp();
//       JSONArray array = JSONObject.parseArray(result);
//       if(array != null && array.size() > 0){
//           return ResponseUtil.ok(JSONObject.toJSONString(array.get(0)));
//       }
       return ResponseUtil.ok(result);
    }

    @RequestMapping("modifydhcp")
    public Result internet(@RequestBody Internet internet){
        String result = this.dhcpService.modifydhcp(internet);
        return ResponseUtil.ok(result);
    }

    @GetMapping("/dhcp")
    public void dhcp() throws FileNotFoundException {
        // 通过ClassLoader读取resources下的文件
//
        InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("./dhcpd/dhcpd.leases");
//        File file = new File("/var/lib/dhcpd/dhcpd.leases");
//        FileInputStream inputStream = new FileInputStream(file);
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                Map<String, String> data = null;
                List<Map<String, String>> dataList = new ArrayList();
                while ((line = reader.readLine()) != null) {
                    if(StringUtil.isNotEmpty(line)){
                        line = line.trim();
                        String key = DhcpUtils.getKey(line);
                        if(StringUtil.isNotEmpty(key)){
                            if(key.equals("lease")){
                                if(data != null){
                                    dataList.add(data);
                                }
                                data = new HashMap();
                            }
                            DhcpUtils.parseValue(key, line, data);
                        }

                    }

                }
                // 最后一个
                if(data != null && StringUtils.isNotBlank(data.get("lease"))){
                    dataList.add(data);
                }

                if(dataList.size() > 0){
//                    System.out.println(dataList);
//                    List l =dataList.stream().map(e -> e.keySet().stream()
//                            .map(r -> r.contains(" ") ? r.replaceAll(" ", "_") : r)).collect(Collectors.toList());
//                    System.out.println(JSONObject.toJSONString(l));

                    for (Map<String, String> map : dataList) {
                        Map<String, String> modifiedMap = new HashMap();
                        Set<Map.Entry<String, String>> set =  map.entrySet();
                        for (Map.Entry<String, String> entry : set) {

                            if(entry.getKey().contains(" ")){
                                modifiedMap.put(entry.getKey().replaceAll(" ", "_"), entry.getValue());
                            }else if(entry.getKey().contains("-")){
                                modifiedMap.put(entry.getKey().replaceAll("-", "_"), entry.getValue());
                            } else{
                                modifiedMap.put(entry.getKey(), entry.getValue());
                            }
                        }
                        Dhcp dhcp = new Dhcp();
                        BeanMap beanMap = BeanMap.create(dhcp);
                        beanMap.putAll(modifiedMap);
                        dhcpService.save(dhcp);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static Map<String,Object> initMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("lease", "张三");
        map.put("starts", "张三");
        map.put("ends", "张三");
        return map;
    }

    @Test
    public void mapToObj1(){
        Map<String,Object> map = initMap();
        Dhcp dhcp = new Dhcp();
//        BeanMap beanMap = BeanMap.create(user);
//        beanMap.putAll(map);

        BeanUtils.copyProperties(map, dhcp);
        System.out.println(dhcp);

        Dhcp d = JSONObject.parseObject(JSONObject.toJSONString(map), Dhcp.class);
        System.out.println(d);
    }

//   @GetMapping("/dhcp")
//    public void test(){
//        // 通过ClassLoader读取resources下的文件
//        InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("./dhcpd/dhcpd.leases");
//        if (inputStream != null) {
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//                String line;
//                Map data = null;
//                List dataList = new ArrayList();
//                while ((line = reader.readLine()) != null) {
//                    if(StringUtil.isNotEmpty(line)){
//                        line = line.trim();
//                        String key = DhcpUtils.getKey(line);
//                        if(StringUtil.isNotEmpty(key)){
//                            if(key.equals("lease")){
//                                if(data != null){
//                                    dataList.add(data);
//                                }
//                                data = new HashMap();
//                            }
//                            DhcpUtils.parseValue(key, line, data);
//                        }
//
//                    }
//
//                }
//                System.out.println(dataList);
//                Dhcp dhcp = new Dhcp();
//                dhcp.setDhcp(JSONObject.toJSONString(dataList));
//
//                dhcpService.save(dhcp);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }


    String template = "" +
                "# 公司内网"
            +
            "\n"
            +
            "subnet 192.168.5.0 netmask 255.255.255.0 {" +
            "\n"
            +
            "        option domain-name-servers 223.5.5.5;" +
            "\n"
            +
            "        range 192.168.5.200 192.168.5.220;" +
            "\n"
            +
            "        option routers 192.168.5.1;" +
            "\n"
            +
            "        }";

//    String name = "";
//    String subnetIp = "";
//    String subnetMask = "";
//    String defaultGatewayIp = "";
//    String subnetAddresses = "subnet " + subnetIp +" netmask " + subnetMask+" {";
//    String defaultGateway = "option routers" + defaultGatewayIp;


    public static void main(String[] args) throws IllegalAccessException {
        AddressPool addressPool1 = new AddressPool();
        addressPool1.setName("公司内网1");
        addressPool1.setSubnetAddresses("192.168.5.101/24");
        addressPool1.setDefaultGateway("192.168.5.101");
        addressPool1.setDNS("192.168.5.101");
//        LinkedHashMap map = JSON.parseObject(JSON.toJSONString(addressPool1), LinkedHashMap.class);
//        Collection<Object> values = map.values();
//        for (Object value: values){
//            System.out.println(value);
//        }


        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        Class<?> clazz = addressPool1.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(addressPool1);
            map.put(fieldName, value);
        }
        Collection<Object> values = map.values();
        for (Object value: values){
            System.out.println(value);
        }
    }

    // write
    @Test
    public void write(){
        AddressPool addressPool1 = new AddressPool();
        addressPool1.setName("公司内网1");
        addressPool1.setSubnetAddresses("192.168.5.101/24");
        addressPool1.setDefaultGateway("192.168.5.101");
        addressPool1.setDNS("192.168.5.101");
        System.out.println(addressPool1.toString());

        AddressPool addressPool2 = new AddressPool();
        addressPool2.setName("公司内网2");
        addressPool2.setDefaultGateway("192.168.5.101");
        addressPool2.setDNS("192.168.5.101");
        addressPool2.setSubnetAddresses("192.168.5.101/24");
//        System.out.println(addressPool2.toString());

        List<AddressPool> list = new ArrayList<>();
        list.add(addressPool1);
        list.add(addressPool2);

        // 使用流式操作进行过滤并输出结果
        List<AddressPool> filteredEntities = list.stream()
                .filter(e -> e != null && !e.getName().isEmpty()) // 这里根据需要自定义判断条件
                .collect(Collectors.toList());
        try {
            // 创建一个 FileOutputStream 对象，指定文件路径
            FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp.txt");

            for (AddressPool entity : filteredEntities) {
//                System.out.println(JSONObject.toJSONString(entity));
                // 乱序
//                String txt = JSONObject.toJSONString(entity);
//                LinkedHashMap<String, Object> map = JSONObject.parseObject(txt, (Type) LinkedHashMap.class);
//
//                Collection<Object> values = map.values();
//                for (Object value: values){
//                    System.out.println(value);
//                    byte[] bytes = value.toString().getBytes();
//                    fos.write(bytes);
//                }
                // 有序
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                Class<?> clazz = addressPool1.getClass();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object value = field.get(addressPool1);
                    map.put(fieldName, value);
                }
                Collection<Object> values = map.values();
                for (Object value: values){
                    if(value != null){
                        System.out.println(value);
                        byte[] bytes = value.toString().getBytes();
                        fos.write(bytes);
                    }
                }

                fos.write("}\n\n".getBytes());
            }
                // 关闭流
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }


//    public static void main(String[] args) {
//        // 获取实体类
//        Class<?> entityClass = AddressPool.class;
//
//        // 遍历实体类的方法
//        Method[] methods = entityClass.getDeclaredMethods();
//        for (Method method : methods) {
//            System.out.println("方法名称：" + method.getName());
//            System.out.println("返回类型：" + method.getReturnType());
//            System.out.println("参数个数：" + method.getParameterCount());
//            System.out.println("参数类型：");
//            for (Class<?> parameterType : method.getParameterTypes()) {
//                System.out.println("  " + parameterType);
//            }
//        }
//    }
}
