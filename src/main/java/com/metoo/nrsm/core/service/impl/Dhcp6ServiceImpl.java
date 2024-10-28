package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.dto.Dhcp6Dto;
import com.metoo.nrsm.core.mapper.Dhcp6Mapper;
import com.metoo.nrsm.core.service.IDhcp6HistoryService;
import com.metoo.nrsm.core.service.IDhcp6Service;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.dhcp.Dhcp6Utils;
import com.metoo.nrsm.entity.Dhcp6;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.*;
import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-16 11:14
 */
@Service
@Transactional
public class Dhcp6ServiceImpl implements IDhcp6Service {

    @Autowired
    private Dhcp6Mapper dhcp6Mapper;
    @Autowired
    private IDhcp6HistoryService dhcp6historyService;

    @Override
    public Dhcp6 selectObjById(Long id) {
        return this.dhcp6Mapper.selectObjById(id);
    }

    @Override
    public Dhcp6 selectObjByLease(String lease) {
        return this.dhcp6Mapper.selectObjByLease(lease);
    }

    @Override
    public Page<Dhcp6> selectConditionQuery(Dhcp6Dto dto) {
        if(dto == null){
            dto = new Dhcp6Dto();
        }
        Page<Dhcp6> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.dhcp6Mapper.selectConditionQuery(dto);
        return page;
    }

    @Override
    public List<Dhcp6> selectObjByMap(Map params) {
        return this.dhcp6Mapper.selectObjByMap(params);
    }

    @Override
    public boolean save(Dhcp6 instance) {
        if(instance.getId() == null){
            try {
                this.dhcp6Mapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }else{
            try {
                this.dhcp6Mapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean update(Dhcp6 instance) {
        try {
            this.dhcp6Mapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            this.dhcp6Mapper.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean truncateTable() {
        try {
            this.dhcp6Mapper.truncateTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTable() {
        try {
            this.dhcp6Mapper.deleteTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void gather(Date time)  {
        try {

            this.deleteTable();

            InputStream inputStream = null;
            if (Global.env.equals("prod")) {
                File file = new File("/var/lib/dhcp/dhcpd6.leases");
                inputStream = new FileInputStream(file);
            } else if ("dev".equals(Global.env)) {
                inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("./dhcpd/dhcpd6.leases");
            }
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

                    if(dataList.size() > 0){
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
                            Dhcp6 dhcp6 = new Dhcp6();
                            dhcp6.setAddTime(time);
                            BeanMap beanMap = BeanMap.create(dhcp6);
                            beanMap.putAll(modifiedMap);
                            this.save(dhcp6);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.dhcp6historyService.batchInsert();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

//    @Override
//    public void gather(Date time)  {
//        try {
//            this.truncateTable();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return;
//        }
//        InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("./dhcpd/dhcpd6.leases");
//        if (inputStream != null) {
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//                String line;
//                Map<String, String> data = null;
//                List<Map<String, String>> dataList = new ArrayList();
//                while ((line = reader.readLine()) != null) {
//                    if (StringUtil.isNotEmpty(line)) {
//                        line = line.trim();
//                        String key = Dhcp6Utils.getKey(line);
//                        if (StringUtil.isNotEmpty(key)) {
//                            if (key.equals("ia-na")) {
//                                if (data != null) {
//                                    dataList.add(data);
//                                }
//                                data = new HashMap();
//                            }
//                            Dhcp6Utils.parseValue(key, line, data);
//                        }
//
//                    }
//
//                }
//                // 最后一个
//                if (data != null && MyStringUtils.isNotBlank(data.get("ia-na"))) {
//                    dataList.add(data);
//                }
//
//                if(dataList.size() > 0){
//                    for (Map<String, String> map : dataList) {
//                        Map<String, String> modifiedMap = new HashMap();
//                        Set<Map.Entry<String, String>> set =  map.entrySet();
//                        for (Map.Entry<String, String> entry : set) {
//                            if(entry.getKey().contains(" ")){
//                                modifiedMap.put(entry.getKey().replaceAll(" ", "_"), entry.getValue());
//                            }else if(entry.getKey().contains("-")){
//                                modifiedMap.put(entry.getKey().replaceAll("-", "_"), entry.getValue());
//                            } else{
//                                modifiedMap.put(entry.getKey(), entry.getValue());
//                            }
//                        }
//                        Dhcp6 dhcp6 = new Dhcp6();
//                        BeanMap beanMap = BeanMap.create(dhcp6);
//                        beanMap.putAll(modifiedMap);
//                        this.save(dhcp6);
//                    }
//                }
//                this.dhcp6historyService.batchInsert();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
