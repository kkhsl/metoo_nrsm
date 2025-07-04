package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONArray;
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
    public Result list(@RequestBody DhcpDto dto) {
        Page<Dhcp> page = this.dhcpService.selectConditionQuery(dto);
        if (page.getResult().size() > 0) {
            return ResponseUtil.ok(new PageInfo<Dhcp>(page));
        }
        return ResponseUtil.ok();
    }

    @GetMapping("getdhcp")
    public Result internet() {
        String result = this.dhcpService.getdhcp();
//       JSONArray array = JSONObject.parseArray(result);
//       if(array != null && !array.isEmpty()){
//           return ResponseUtil.ok(JSONObject.toJSONString(array.get(0)));
//       }
        return ResponseUtil.ok(result);
    }

    @RequestMapping("checkdhcpd")
    public Result checkdhcpd() {
        Map result = new HashMap();
        try {
            String dhcpd = this.dhcpService.checkdhcpd("dhcpd");
            result.put("dhcpd", dhcpd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String dhcpd6 = this.dhcpService.checkdhcpd("dhcpd6");
            result.put("dhcpd6", dhcpd6);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseUtil.ok(result);
    }

    @RequestMapping("modifydhcp")
    public Result internet(@RequestBody Internet internet) {
        String result = this.dhcpService.modifydhcp(internet);
        try {
            if (Boolean.valueOf(internet.getV4status())) {
                this.dhcpService.dhcpdop("restart", "dhcpd");
            } else {
                this.dhcpService.dhcpdop("stop", "dhcpd");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (Boolean.valueOf(internet.getV6status())) {
                this.dhcpService.dhcpdop("restart", "dhcpd6");
            } else {
                this.dhcpService.dhcpdop("stop", "dhcpd6");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    if (StringUtil.isNotEmpty(line)) {
                        line = line.trim();
                        String key = DhcpUtils.getKey(line);
                        if (StringUtil.isNotEmpty(key)) {
                            if (key.equals("lease")) {
                                if (data != null) {
                                    dataList.add(data);
                                }
                                data = new HashMap();
                            }
                            DhcpUtils.parseValue(key, line, data);
                        }

                    }

                }
                // 最后一个
                if (data != null && StringUtils.isNotBlank(data.get("lease"))) {
                    dataList.add(data);
                }

                if (dataList.size() > 0) {
//                    System.out.println(dataList);
//                    List l =dataList.stream().map(e -> e.keySet().stream()
//                            .map(r -> r.contains(" ") ? r.replaceAll(" ", "_") : r)).collect(Collectors.toList());
//                    System.out.println(JSONObject.toJSONString(l));

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

//   @GetMapping("/dhcp")
//    public void TestAbstrack(){
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
////                        if(StringUtil.isNotEmpty(key)){
////                            if(key.equals("lease")){
////                                if(data != null){
////                                    dataList.add(data);
////                                }
////                                data = new HashMap();
////                            }
////                            DhcpUtils.parseValue(key, line, data);
////                        }
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


}
