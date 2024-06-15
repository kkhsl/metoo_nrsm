package com.metoo.nrsm.core.utils.io.address.pool;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.IAddressPoolFixedService;
import com.metoo.nrsm.core.vo.AddressPoolFixedVO;
import com.metoo.nrsm.core.vo.AddressPoolVO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-10 11:24
 */
public class AddressPoolIpv4ConcurrentUtil {

    private static AddressPoolIpv4ConcurrentUtil addressPoolIpv4ConcurrentUtil = new AddressPoolIpv4ConcurrentUtil();

    private AddressPoolIpv4ConcurrentUtil(){};

    public static AddressPoolIpv4ConcurrentUtil getInstance(){
        return addressPoolIpv4ConcurrentUtil;
    }

    public synchronized boolean write(List<AddressPoolVO> addressPoolVOList){
        if(addressPoolVOList.size() > 0){
            IAddressPoolFixedService addressPoolFixedService = (IAddressPoolFixedService) ApplicationContextUtils.getBean("addressPoolFixedServiceImpl");
            // 使用流式操作进行过滤并输出结果
            List<AddressPoolVO> filteredEntities = addressPoolVOList.stream()
                    .filter(e -> e != null && !e.getName().isEmpty()) // 这里根据需要自定义判断条件
                    .collect(Collectors.toList());
            try {
                // 创建一个 FileOutputStream 对象，指定文件路径
//                FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp.txt");
                FileOutputStream fos = new FileOutputStream(new File("/etc/dhcp/dhcpd.conf"));

                fos.getChannel().position(0).truncate(0); // 清空文件内容

                Map params = new HashMap();

                for (AddressPoolVO entity : filteredEntities) {
                    // 有序
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    Class<?> clazz = entity.getClass();
                    for (Field field : clazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        Object value = field.get(entity);
                        if(value != null){
                            map.put(fieldName, value);
                        }
                    }
                    Collection<Object> values = map.values();
                    for (Object value: values){
                        if(value != null && !value.equals("")){
                            byte[] bytes = value.toString().getBytes();
                            fos.write(bytes);
                        }
                    }

                    // 固定地址池
                    if(map.get("subnetAddresses") != null && !"".equals(map.get("subnetAddresses"))){
                        String subnetAddresses = String.valueOf(map.get("subnetAddresses"));
                        subnetAddresses = subnetAddresses.replaceAll("\\s*|\r|\n|\t|\\n","").replaceAll("subnet|\\{","").replaceAll("netmask", "/");
                        String subnet = subnetAddresses.substring(0, subnetAddresses.indexOf("/"));
                        String subnetMask = subnetAddresses.substring(subnetAddresses.indexOf("/") + 1);
                        params.clear();
                        params.put("subnet", subnet);
                        params.put("mask", subnetMask);
                        List<AddressPoolFixedVO> addressPoolFixedVOList = addressPoolFixedService.selectObjToVOByMap(params);

                        // 使用流式操作进行过滤并输出结果
                        List<AddressPoolFixedVO> addressPoolFixeds = addressPoolFixedVOList.stream()
                                .filter(e -> e != null && !e.getHost().isEmpty()) // 这里根据需要自定义判断条件
                                .collect(Collectors.toList());
                        if(filteredEntities.size() > 0){
                            for (AddressPoolFixedVO addressPoolFixed : addressPoolFixeds) {
                                // 有序
                                LinkedHashMap<String, Object> map_fixed = new LinkedHashMap<>();
                                Class<?> clazz_fixed = addressPoolFixed.getClass();
                                for (Field field : clazz_fixed.getDeclaredFields()) {
                                    field.setAccessible(true);
                                    String fieldName = field.getName();
                                    Object value = field.get(addressPoolFixed);
                                    if(value != null){
                                        map_fixed.put(fieldName, value);
                                    }
                                }
                                Collection<Object> values_fixed = map_fixed.values();
//                                String annotation = "#" + map_fixed.get("host").toString().getBytes();
                                for (Object value: values_fixed){
                                    if(value != null && !value.equals("")){
                                        value = "        " + value;
                                        byte[] bytes = value.toString().getBytes();
                                        fos.write(bytes);
                                    }
                                }
                                fos.write("         }\n\n".getBytes());
                            }
                        }
                    }

                    fos.write("}\n\n".getBytes());
                }

                // 关闭流
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();  return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String a = "subnet192.168.6.0netmask255.255.255.0{";
        System.out.println(a.replaceAll("subnet|\\{",""));

        System.out.println(a.replaceAll("subnet|\\{","").replaceAll("netmask", "/"));

    }
}
