package com.metoo.nrsm.core.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.dto.LocalDataDTO;
import com.metoo.nrsm.core.dto.LocalZoneDTO;
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
    @Transactional()
    public boolean add(UnboundDTO instance) {

        Unbound unbound = this.selectObjByOne(Collections.emptyMap());
        if(unbound == null){
            unbound = new Unbound();
            try {
                String localData = new ObjectMapper().writeValueAsString(instance.getLocalData());
                unbound.setLocalData(localData);
                String localZone = new ObjectMapper().writeValueAsString(instance.getLocalZone());
                unbound.setLocalZone(localZone);

                // 动态赋值
                for (LocalZoneDTO zone : instance.getLocalZone()) {
                    String zoneName = zone.getZoneName();
                    String zoneType = zone.getZoneType();
                    String local = new ObjectMapper().writeValueAsString(zone.getLocalData());
                    unbound.setZoneName(zoneName);
                    unbound.setZoneType(zoneType);
                    unbound.setLocalData(local);
                    for (LocalDataDTO data : zone.getLocalData()) {
                        String hostName = data.getHostName();
                        String recordType = data.getRecordType();
                        String mappedAddress = data.getMappedAddress();
                        unbound.setHostName(hostName);
                        unbound.setRecordType(recordType);
                        unbound.setMappedAddress(mappedAddress);
                    }
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return this.save(unbound);
        }else{
            try {
                String localData = new ObjectMapper().writeValueAsString(instance.getLocalData());
                unbound.setLocalData(localData);
                String localZone = new ObjectMapper().writeValueAsString(instance.getLocalZone());
                unbound.setLocalZone(localZone);

                // 动态赋值
                for (LocalZoneDTO zone : instance.getLocalZone()) {
                    String zoneName = zone.getZoneName();
                    String zoneType = zone.getZoneType();
                    String local = new ObjectMapper().writeValueAsString(zone.getLocalData());
                    unbound.setZoneName(zoneName);
                    unbound.setZoneType(zoneType);
                    unbound.setLocalData(local);
                    for (LocalDataDTO data : zone.getLocalData()) {
                        String hostName = data.getHostName();
                        String recordType = data.getRecordType();
                        String mappedAddress = data.getMappedAddress();
                        unbound.setHostName(hostName);
                        unbound.setRecordType(recordType);
                        unbound.setMappedAddress(mappedAddress);
                    }
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

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
    @Transactional()
    public boolean addDNS(UnboundDTO instance) {

        Unbound unbound = this.selectObjByOne(Collections.emptyMap());
        if(unbound == null){
            unbound = new Unbound();
            try {
                String forwardAddressJson = new ObjectMapper().writeValueAsString(instance.getForwardAddress());
                unbound.setForwardAddress(forwardAddressJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return this.saveDNS(unbound);
        }else{
            try {
                String forwardAddressJson = new ObjectMapper().writeValueAsString(instance.getForwardAddress());
                unbound.setForwardAddress(forwardAddressJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            try {
                unbound.setUpdateTime(new Date());
                int i = this.unboundMapper.update(unbound);
                boolean flag = writeUnboundDNS();
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
            Unbound unbound = this.unboundMapper.selectObjByOne(Collections.EMPTY_MAP);
            if (unbound.getLocalZone()!=null){
                UnboundConfUtil.deleteConfigFile(Global.unboundPath, unbound);
                this.unboundMapper.delete(id);
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteDNS(Long id) {
        try {
            Unbound unbound = this.unboundMapper.selectObjByOne(Collections.EMPTY_MAP);
            UnboundDTO unboundDTO = new UnboundDTO();
            unboundDTO.setForwardAddress(new ArrayList<>());
            if (unbound.getForwardAddress()!=null){
                addDNS(unboundDTO);
                this.unboundMapper.deleteDNS(id);
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteAll(Long id) {
        try {
            this.unboundMapper.deleteAll(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



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

    @Override
    @Transactional(rollbackFor = Exception.class)  // 强制回滚所有异常
    public boolean saveDNS(Unbound instance) {
        if(instance.getId() == null || instance.getId().equals("")){
            instance.setAddTime(new Date());
            instance.setUpdateTime(new Date());
            try {
                int i = this.unboundMapper.save(instance);
                boolean flag = writeUnboundDNS();
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
                boolean flag = writeUnboundDNS();
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
    public boolean writeUnboundDNS() throws Exception {
        Unbound unbound = this.unboundMapper.selectObjByOne(Collections.EMPTY_MAP);
        boolean flag = UnboundConfUtil.updateConfigDNSFile(Global.unboundPath, unbound);
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











}
