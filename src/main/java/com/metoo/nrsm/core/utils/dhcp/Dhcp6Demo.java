package com.metoo.nrsm.core.utils.dhcp;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.entity.Dhcp6;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.repository.init.ResourceReader;

import java.io.*;
import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-31 14:34
 */
public class Dhcp6Demo {

    public static void main(String[] args) {
        InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("./dhcpd/dhcpd6.leases");
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                Map<String, String> data = null;
                List<Map<String, String>> dataList = new ArrayList();
                while ((line = reader.readLine()) != null) {
                    if (StringUtil.isNotEmpty(line)) {
                        line = line.trim();
                        String key = Dhcp6Utils.getKey(line);
                        if (StringUtil.isNotEmpty(key)) {
                            if (key.equals("ia-na")) {
                                if (data != null) {
                                    dataList.add(data);
                                }
                                data = new HashMap();
                            }
                            Dhcp6Utils.parseValue(key, line, data);
                        }

                    }

                }
                // 最后一个
                if (data != null && StringUtils.isNotBlank(data.get("ia-na"))) {
                    dataList.add(data);
                }

                if (dataList.size() > 0) {
                    for (Map<String, String> map : dataList) {
                        Map<String, String> modifiedMap = new HashMap();
                        Set<Map.Entry<String, String>> set = map.entrySet();
                        for (Map.Entry<String, String> entry : set) {
                            if (entry.getKey().contains(" ")) {
                                modifiedMap.put(entry.getKey().replaceAll(" ", "_"), entry.getValue());
                            } else if (entry.getKey().contains("-")) {
                                modifiedMap.put(entry.getKey().replaceAll("-", "_"), entry.getValue());
                            } else {
                                modifiedMap.put(entry.getKey(), entry.getValue());
                            }
                        }
                        Dhcp6 dhcp6 = new Dhcp6();
                        BeanMap beanMap = BeanMap.create(dhcp6);
                        beanMap.putAll(modifiedMap);
                        System.out.println(dhcp6);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
