package com.metoo.nrsm.core.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.core.mapper.UnboundMapper;
import com.metoo.nrsm.core.service.IUnboundService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.unbound.UnboundConfUtil;
import com.metoo.nrsm.entity.Unbound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Service
public class UnboundServiceImpl implements IUnboundService {

    @Resource
    private UnboundMapper unboundMapper;

    @Override
    public Unbound selectObjByOne(Map params) {
        return this.unboundMapper.selectObjByOne(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)  // 强制回滚所有异常
    public boolean save(Unbound instance) {
        if(instance.getId() == null || instance.getId().equals("")){
            instance.setAddTime(new Date());
            instance.setUpdateTime(new Date());
            try {
                int i = this.unboundMapper.save(instance);
                boolean flag = writeUnbound();
                if(flag && i >= 1){
                    return true;
                }
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
        }else{
            try {
                instance.setUpdateTime(new Date());
                int i = this.unboundMapper.update(instance);
                boolean flag = writeUnbound();
                if(flag && i >= 1){
                    return true;
                }
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
        }
    }

    public boolean writeUnbound() throws Exception {
        Unbound unbound = this.unboundMapper.selectObjByOne(Collections.EMPTY_MAP);
        boolean flag = UnboundConfUtil.updateConfigFile(Global.unboundPath, unbound);
        if (!flag) {
            throw new IOException("Failed to update config file");
        }
        return flag;
    }

    @Override
    @Transactional()
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
                int i = this.unboundMapper.update(unbound);
                boolean flag = writeUnbound();
                if(flag && i >= 1){
                    return true;
                }
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
