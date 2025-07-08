package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.mapper.RouteTableMapper;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPParamFactory;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.RouteEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RouteServiceImpl {

    private static final int BATCH_SIZE = 200;

    @Autowired
    private RouteTableMapper routeTableMapper;

    @Autowired
    private NetworkElementServiceImpl networkElementService;


    /**
     * 处理单个设备的路由数据
     */
    @Transactional
    public void processDeviceRoutes(NetworkElement networkElement) {
        String deviceIp = networkElement.getIp();

        // 1. 清空该设备现有路由
        routeTableMapper.deleteByDeviceIp(deviceIp);

        // 2. 获取路由数据
        String jsonResult = SNMPv3Request.getRoute(SNMPParamFactory.createSNMPParam(networkElement));
        if (jsonResult == null || jsonResult.isEmpty()) {
            return;
        }

        // 3. 解析JSON
        List<RouteEntry> routeEntries = parseJsonToRouteEntries(deviceIp, jsonResult);
        if (routeEntries.isEmpty()) {
            return;
        }

        // 4. 分批插入数据
        insertInBatches(routeEntries);
    }

    /**
     * 批量插入路由数据
     */
    private void insertInBatches(List<RouteEntry> entries) {
        // 分批处理
        try {
            for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, entries.size());
                List<RouteEntry> subList = entries.subList(i, end);
                routeTableMapper.batchInsertRoutes(subList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析JSON到路由实体列表
     */
    private List<RouteEntry> parseJsonToRouteEntries(String deviceIp, String jsonData) {
        List<RouteEntry> entries = new ArrayList<>();

        try {
            JSONArray jsonArray = JSON.parseArray(jsonData);

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                RouteEntry entry = new RouteEntry();
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



    public List<RouteEntry> getDeviceRouteByUuid(String uuid) {
        Map params = new HashMap();
        params.put("uuid", uuid);
        List<NetworkElement> networkElements = networkElementService.selectObjByMap(params);
        if(networkElements.size() > 0){
            NetworkElement networkElement = networkElements.get(0);
            List<RouteEntry> routes = routeTableMapper.selectObjByDeviceUuid(networkElement.getIp());
            if(routes.size() > 0){
                return routes;
            }
        }
        return new ArrayList<>();
    }
}