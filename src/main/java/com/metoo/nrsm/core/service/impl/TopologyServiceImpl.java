package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.TopologyDTO;
import com.metoo.nrsm.core.mapper.TopologyMapper;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.IPortService;
import com.metoo.nrsm.core.service.ITopologyService;
import com.metoo.nrsm.core.utils.collections.ListSortUtil;
import com.metoo.nrsm.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Lazy
@Service
@Transactional
public class TopologyServiceImpl implements ITopologyService {

    @Autowired
    private TopologyMapper topologyMapper;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private IPortService portService;

    @Override
    public Topology selectObjById(Long id) {
        return this.topologyMapper.selectObjById(id);
    }

    @Override
    public Topology selectObjBySuffix(String name) {
        return this.topologyMapper.selectObjBySuffix(name);
    }

    @Override
    public Page<Topology> selectConditionQuery(TopologyDTO instance) {
        if(instance == null){
            instance = new TopologyDTO();
        }
        Page<Topology> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());

        this.topologyMapper.selectConditionQuery(instance);
        return page;
    }

    @Override
    public List<Topology> selectObjByMap(Map params) {
        return this.topologyMapper.selectObjByMap(params);
    }

    @Override
    public List<Topology> selectObjHistoryByMap(Map params) {
        return this.topologyMapper.selectObjHistoryByMap(params);
    }


    @Override
    public List<Topology> selectTopologyByMap(Map params) {
        return this.topologyMapper.selectTopologyByMap(params);
    }

    @Override
    public int save(Topology instance) {
        if(instance.getId() == null){
            instance.setAddTime(new Date());
        }else{
            instance.setUpdateTime(new Date());
        }
        if(instance.getContent() != null && !instance.getContent().equals("")){
            // 解析content 并写入uuid
            Object content = this.writerUuid(instance.getContent());
            instance.setContent(content);
        }
        User user = ShiroUserHolder.currentUser();
        instance.setUnitId(user.getUnitId());
        if(instance.getId() == null){
            try {
               int i = this.topologyMapper.save(instance);
                if(i >= 1){
                    try {
                        Calendar cal = Calendar.getInstance();
                        instance.setAddTime(cal.getTime());
                        this.saveHistory(instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return instance.getId().intValue();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {
                int i = this.topologyMapper.update(instance);
                if(i >= 1){
                    try {
                        Calendar cal = Calendar.getInstance();
                        instance.setAddTime(cal.getTime());
                        this.saveHistory(instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return i;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    public int saveHistory(Topology instance) {
        if(instance.getId() == null){
            instance.setAddTime(new Date());
        }else{
            instance.setUpdateTime(new Date());
        }
        if(instance.getContent() != null && !instance.getContent().equals("")){
            // 解析content 并写入uuid
            Object content = this.writerUuid(instance.getContent());
            instance.setContent(content);
        }
        if(instance.getId() == null){
            try {
                int i = this.topologyMapper.saveHistory(instance);
                if(i >= 1){
                    try {
                        Calendar cal = Calendar.getInstance();
                        instance.setAddTime(cal.getTime());
                        this.topologyMapper.saveHistory(instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return instance.getId().intValue();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {
                int i = this.topologyMapper.update(instance);
                if(i >= 1){
                    try {
                        Calendar cal = Calendar.getInstance();
                        instance.setAddTime(cal.getTime());
                        this.topologyMapper.saveHistory(instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return i;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    // 为拓扑图连线增加Uuid
    public Object writerUuid(Object param){
        if (param != null) {
            Map content = JSONObject.parseObject(param.toString(), Map.class);
            JSONArray links = this.getLinks(content);
            if(links.size() > 0){
                List list = this.setUuid(links);
                content.put("links", list);
                return JSON.toJSONString(content);
            }
        }
        return param;
    }

    public JSONArray getLinks(Map content){
        if (content != null) {
            if (content.get("links") != null) {
                JSONArray links = JSONArray.parseArray(content.get("links").toString());
                if (links.size() > 0) {
                    return links;
                }
            }
        }
        return new JSONArray();
    }

    public List setUuid(JSONArray links){
        List list = new ArrayList();
        for (Object object : links) {
            Map link = JSONObject.parseObject(object.toString(), Map.class);
            if(link.get("uuid") == null || link.get("uuid").equals("")){
                link.put("uuid", UUID.randomUUID());
            }
            list.add(link);
        }
        return list;
    }

    @Override
    public int update(Topology instance) {
        try {
            return this.topologyMapper.update(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            return this.topologyMapper.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Long copy(Topology instance) {
        try {
            int i = this.topologyMapper.copy(instance);
            if(i >= 1){
                return instance.getId();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Port> getDevicePortsByUuid(String uuid) {
        Map params = new HashMap();
        params.put("uuid", uuid);
        List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
        if(networkElements.size() > 0){
            NetworkElement networkElement = networkElements.get(0);
            List<Port> ports = this.portService.selectObjByDeviceUuid(networkElement.getUuid());
            if(ports.size() > 0){
                return ports;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getTerminalPortsByUuid(String uuid) {
        return null;
    }

}
