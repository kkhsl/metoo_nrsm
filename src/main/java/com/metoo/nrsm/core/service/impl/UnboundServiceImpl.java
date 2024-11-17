package com.metoo.nrsm.core.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.core.mapper.UnboundMapper;
import com.metoo.nrsm.core.service.IUnboundService;
import com.metoo.nrsm.entity.Unbound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UnboundServiceImpl implements IUnboundService {

    @Resource
    private UnboundMapper unboundMapper;

    @Override
    public Unbound selectObjByOne(Map params) {
        return this.unboundMapper.selectObjByOne(params);
    }

    @Override
    public boolean save(Unbound instance) {
        if(instance.getId() == null || instance.getId().equals("")){
            instance.setAddTime(new Date());
            instance.setUpdateTime(new Date());
            try {
                this.unboundMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }else{
            try {
                instance.setUpdateTime(new Date());
                this.unboundMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    //List<String> forwardAddress = new ObjectMapper()
    //                .readValue(config.getForwardAddress(), new TypeReference<List<String>>() {});
    @Override
    public boolean update(UnboundDTO instance) {

        Unbound unbound = this.selectObjByOne(Collections.emptyMap());
        if(unbound == null){
            unbound = new Unbound();
            try {
                String forwardAddressJson = new ObjectMapper().writeValueAsString(instance.getForwardAddress());
                unbound.setForwardAddress(forwardAddressJson);

                String localData = new ObjectMapper().writeValueAsString(instance.getLocalData());

                unbound.setLocalData(localData);

                String localZone = new ObjectMapper().writeValueAsString(instance.getLocalZone());

                unbound.setLocalZone(localZone);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            unbound.setPrivateAddress(instance.getPrivateAddress());
            return this.save(unbound);
        }else{
            try {
                String forwardAddressJson = new ObjectMapper().writeValueAsString(instance.getForwardAddress());
                unbound.setForwardAddress(forwardAddressJson);

                String localData = new ObjectMapper().writeValueAsString(instance.getLocalData());

                unbound.setLocalData(localData);

                String localZone = new ObjectMapper().writeValueAsString(instance.getLocalZone());

                unbound.setLocalZone(localZone);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            unbound.setPrivateAddress(instance.getPrivateAddress());
            try {
                unbound.setUpdateTime(new Date());
                this.unboundMapper.update(unbound);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            this.unboundMapper.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
