package com.metoo.nrsm.core.utils.io.address.pool;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.IAddressPoolFixedService;
import com.metoo.nrsm.core.service.IAddressPoolV6FixedService;
import com.metoo.nrsm.core.utils.ip.Ipv6.IPv6SubnetCheck;
import com.metoo.nrsm.core.utils.ip.Ipv6.Ipv6Utils;
import com.metoo.nrsm.core.vo.AddressPoolFixedVO;
import com.metoo.nrsm.core.vo.AddressPoolIpv6VO;
import com.metoo.nrsm.core.vo.AddressPoolV6FixedVO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.entity.AddressPoolV6Fixed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

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
public class AddressPoolIpv6ConcurrentUtil {

    private static AddressPoolIpv6ConcurrentUtil addressPoolIpv6ConcurrentUtil = new AddressPoolIpv6ConcurrentUtil();

    private AddressPoolIpv6ConcurrentUtil() {
    }

    ;

    public static AddressPoolIpv6ConcurrentUtil getInstance() {
        return addressPoolIpv6ConcurrentUtil;
    }

    public synchronized boolean write(List<AddressPoolIpv6VO> addressPoolIpv6VOS) {
        IAddressPoolV6FixedService addressPoolV6FixedService = (IAddressPoolV6FixedService) ApplicationContextUtils.getBean("addressPoolV6FixedServiceImpl");

        if (addressPoolIpv6VOS.size() > 0) {
            AddressPoolIpv6VO default_ipv6 = new AddressPoolIpv6VO();
            default_ipv6.getDefault();
            addressPoolIpv6VOS.add(0, default_ipv6);
            // 使用流式操作进行过滤并输出结果
            List<AddressPoolIpv6VO> filteredEntities = addressPoolIpv6VOS.stream()
                    .filter(e -> e != null) // 这里根据需要自定义判断条件  && !e.getName().isEmpty()
                    .collect(Collectors.toList());
            try {
                // 创建一个 FileOutputStream 对象，指定文件路径
//                FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp6.txt");
                FileOutputStream fos = new FileOutputStream(new File("/etc/dhcp/dhcpd6.conf"));

                fos.getChannel().position(0).truncate(0); // 清空文件内容

                for (AddressPoolIpv6VO entity : filteredEntities) {
                    // 有序
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    Class<?> clazz = entity.getClass();
                    for (Field field : clazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        Object value = field.get(entity);
                        if (value != null) {
                            map.put(fieldName, value);
                        }
                    }
                    Collection<Object> values = map.values();
                    for (Object value : values) {
                        if (value != null && !value.equals("")) {
                            byte[] bytes = value.toString().getBytes();
                            fos.write(bytes);
                        }
                    }
                    // 固定地址池
                    if (map.get("subnetAddresses") != null && !"".equals(map.get("subnetAddresses"))) {
                        String subnetAddresses = String.valueOf(map.get("subnetAddresses"));
                        subnetAddresses = subnetAddresses.replaceAll("\\s*|\r|\n|\t|\\n", "")
                                .replaceAll("subnet6|\\{", "").replaceAll("netmask", "/");
//                        String subnet = subnetAddresses.substring(0, subnetAddresses.indexOf("/"));
//                        String subnetMask = subnetAddresses.substring(subnetAddresses.indexOf("/") + 1);
                        List<AddressPoolV6Fixed> addressPoolV6Fixeds = addressPoolV6FixedService.selectObjByMap(null);
                        List<AddressPoolV6FixedVO> list = new ArrayList();
                        int index = 1;
                        for (AddressPoolV6Fixed addressPoolV6Fixed : addressPoolV6Fixeds) {
                            if (StringUtil.isNotEmpty(addressPoolV6Fixed.getFixed_address6())) {
                                boolean flag = IPv6SubnetCheck.isInSubnet(addressPoolV6Fixed.getFixed_address6(), subnetAddresses);
                                if (flag) {
                                    AddressPoolV6FixedVO addressPoolV6FixedVO = new AddressPoolV6FixedVO(addressPoolV6Fixed.getHost(), index,
                                            addressPoolV6Fixed.getHost_identifier_option_dhcp6_client_id(), addressPoolV6Fixed.getFixed_address6());
//                                    BeanUtils.copyProperties(addressPoolV6Fixed, addressPoolV6FixedVO);
                                    list.add(addressPoolV6FixedVO);
                                    index++;
                                }
                            }
                        }
                        // 使用流式操作进行过滤并输出结果
                        List<AddressPoolV6FixedVO> addressPoolFixeds = list.stream()
                                .filter(e -> e != null && !e.getHost().isEmpty()) // 这里根据需要自定义判断条件
                                .collect(Collectors.toList());
                        if (filteredEntities.size() > 0) {
                            for (AddressPoolV6FixedVO addressPoolFixed : addressPoolFixeds) {
                                // 有序
                                LinkedHashMap<String, Object> map_fixed = new LinkedHashMap<>();
                                Class<?> clazz_fixed = addressPoolFixed.getClass();
                                for (Field field : clazz_fixed.getDeclaredFields()) {
                                    field.setAccessible(true);
                                    String fieldName = field.getName();
                                    Object value = field.get(addressPoolFixed);
                                    if (value != null) {
                                        map_fixed.put(fieldName, value);
                                    }
                                }
                                Collection<Object> values_fixed = map_fixed.values();
//                                String annotation = "#" + map_fixed.get("host").toString().getBytes();
                                for (Object value : values_fixed) {
                                    if (value != null && !value.equals("")) {
                                        value = "        " + value;
                                        byte[] bytes = value.toString().getBytes();
                                        fos.write(bytes);
                                    }
                                }
                                fos.write("         }\n\n".getBytes());
                            }
                        }
                    }

                    if (StringUtils.isNotEmpty(entity.getName())) {
                        fos.write("}\n\n".getBytes());
                    } else {
                        fos.write("\n".getBytes());
                    }
                }


                // 关闭流
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public static void main(String[] args) {
        String a = "subnet192.168.6.0netmask255.255.255.0{";
        System.out.println(a.replaceAll("subnet|\\{", ""));

        System.out.println(a.replaceAll("subnet|\\{", "").replaceAll("netmask", "/"));

    }
}
