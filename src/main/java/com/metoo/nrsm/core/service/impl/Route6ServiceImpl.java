package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.mapper.Route6TableMapper;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPParamFactory;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.Route6Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class Route6ServiceImpl {
    
    private static final int BATCH_SIZE = 200;
    
    @Autowired
    private Route6TableMapper route6TableMapper;

    @Autowired
    private NetworkElementServiceImpl networkElementService;


    /**
     * 处理单个设备的路由数据
     */
    @Transactional
    public void processDeviceRoutes6(NetworkElement networkElement) {
        String deviceIp = networkElement.getIp();
        
        // 1. 清空该设备现有路由
        route6TableMapper.deleteByDeviceIp(deviceIp);
        
        // 2. 获取路由数据
        String jsonResult = SNMPv3Request.getRoute6(SNMPParamFactory.createSNMPParam(networkElement));
        if (jsonResult == null || jsonResult.isEmpty()) {
            return;
        }
        
        // 3. 解析JSON
        List<Route6Entry> routeEntries = parseJsonToRouteEntries(deviceIp, jsonResult);
        if (routeEntries.isEmpty()) {
            return;
        }
        
        // 4. 分批插入数据
        insertInBatches(routeEntries);
    }

    /**
     * 批量插入路由数据
     */
    private void insertInBatches(List<Route6Entry> entries) {
        // 分批处理
        try {
            for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, entries.size());
                List<Route6Entry> subList = entries.subList(i, end);
                route6TableMapper.batchInsertRoutes(subList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析JSON到路由实体列表
     */
    private List<Route6Entry> parseJsonToRouteEntries(String deviceIp, String jsonData) {
        List<Route6Entry> entries = new ArrayList<>();
        
        try {
            JSONArray jsonArray = JSON.parseArray(jsonData);
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Route6Entry entry = new Route6Entry();
                entry.setTime(new Date());
                entry.setDeviceIp(deviceIp);
                entry.setDestnetwork(obj.getString("Destnetwork"));
                entry.setMask(obj.getString("Mask"));
                entry.setInterfaceName(obj.getString("Interface"));
                entry.setPort(obj.getString("Port"));
                entry.setNexthop(obj.getString("Nexthop"));
                entry.setPreference(obj.getString("Preference"));

                // 处理cost类型(可能为字符串或数字)
                Object costObj = obj.get("Cost");
                if (costObj != null) {
                    entry.setCost(Integer.parseInt(costObj.toString()));
                } else {
                    entry.setCost(0); // 默认值
                }
                
                // 处理type类型
                Object typeObj = obj.get("type");
                if (typeObj != null) {
                    entry.setType(typeObj.toString());
                } else {
                    entry.setType("UNKNOWN"); // 默认值
                }
                
                entries.add(entry);
            }
        } catch (Exception e) {
            // 记录解析错误
            System.err.println("JSON解析错误: " + e.getMessage() + " 设备IP: " + deviceIp);
        }
        
        return entries;
    }

    public List<Route6Entry> getDeviceRouteByUuid(String uuid) {
        Map params = new HashMap();
        params.put("uuid", uuid);
        List<NetworkElement> networkElements = networkElementService.selectObjByMap(params);
        if(networkElements.size() > 0){
            NetworkElement networkElement = networkElements.get(0);
            List<Route6Entry> routes = route6TableMapper.selectObjByDeviceUuid(networkElement.getIp());
            if(routes.size() > 0){
                return routes;
            }
        }
        return new ArrayList<>();
    }


    public void copyDataToRoute6History(){
        route6TableMapper.copyDataToRoute6History();
    }


}