package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.NetworkElementDto;
import com.metoo.nrsm.core.mapper.NetworkElementMapper;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional
public class NetworkElementServiceImpl implements INetworkElementService {

    @Autowired
    private NetworkElementMapper networkElementMapper;

    @Override
    public NetworkElement selectObjById(Long id) {
        return this.networkElementMapper.selectObjById(id);
    }

    @Override
    public NetworkElement selectObjByUuid(String uuid) {
        return this.networkElementMapper.selectObjByUuid(uuid);
    }


    @Override
    public Page<NetworkElement> selectConditionQuery(NetworkElementDto instance) {
        if(instance == null){
            instance = new NetworkElementDto();
        }
        if(instance.getDeleteStatus() == null){
            instance.setDeleteStatus(0);
        }
        Page<NetworkElement> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.networkElementMapper.selectConditionQuery(instance);
        return page;
}

    @Override
    public List<NetworkElement> selectObjByMap(Map params) {
        if(params.get("deleteStatus") == null){
            params.put("deleteStatus", 0);
        }
        if(params.get("deleteStatusList") != null){
            params.remove("deleteStatus");
        }
        return this.networkElementMapper.selectObjByMap(params);
    }

    @Override
    public List<NetworkElement> selectObjAll() {
        return this.networkElementMapper.selectObjAll(null);
    }

    @Override
    public List<NetworkElement> selectObjAllByGather() {
        return this.networkElementMapper.selectObjAllByGather();
    }

    @Override
    public int save(NetworkElement instance) {
        if(instance.getId() == null){
            instance.setAddTime(new Date());
            instance.setUuid(UUID.randomUUID().toString());
//            if(!instance.getDeviceName().contains("NSwitch")){
//                User user = ShiroUserHolder.currentUser();
//                instance.setUserId(user.getId());
//                instance.setUserName(user.getUsername());
//            }
            try {
                return this.networkElementMapper.save(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {
                return this.networkElementMapper.update(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    public int batchInsert(List<NetworkElement> instances) {
        for (NetworkElement instance : instances) {
            instance.setAddTime(new Date());
            instance.setUuid(UUID.randomUUID().toString());
            User user = ShiroUserHolder.currentUser();
            instance.setUserId(user.getId());
            instance.setUserName(user.getUsername());
        }
        try {
            int i = this.networkElementMapper.batchInsert(instances);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int update(NetworkElement instance) {
        try {
            this.networkElementMapper.update(instance);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            this.networkElementMapper.delete(id);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public NetworkElement selectAccessoryByUuid(String uuid) {

        return this.networkElementMapper.selectAccessoryByUuid(uuid);
    }

}
