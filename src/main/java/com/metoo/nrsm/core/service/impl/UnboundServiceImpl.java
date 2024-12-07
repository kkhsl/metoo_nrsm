package com.metoo.nrsm.core.service.impl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UnboundServiceImpl implements IUnboundService {

    @Resource
    private UnboundMapper unboundMapper;


    private String host = "192.168.6.101";
    private String username = "root";
    private String password = "Metoo89745000!";
    private int port = 22;


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
                /*for (LocalZoneDTO zone : instance.getLocalZone()) {
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
                }*/

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
                /*for (LocalZoneDTO zone : instance.getLocalZone()) {
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
                }*/

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
    @Transactional()
    public boolean open(UnboundDTO instance) {

        Unbound unbound = this.selectObjByOne(Collections.emptyMap());
        if(unbound == null){
            unbound = new Unbound();
            unbound.setPrivateAddress(instance.getPrivateAddress());
            return this.openAdress(unbound);
        }else{
            unbound.setPrivateAddress(instance.getPrivateAddress());
            try {
                unbound.setUpdateTime(new Date());
                int i = this.unboundMapper.update(unbound);
                boolean flag = writeUnboundAdress();
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

    @Override
    @Transactional(rollbackFor = Exception.class)  // 强制回滚所有异常
    public boolean openAdress(Unbound instance) {
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

    public boolean writeUnboundAdress() throws Exception {
        Unbound unbound = this.unboundMapper.selectObjByOne(Collections.EMPTY_MAP);
        boolean flag = UnboundConfUtil.updateConfigAdressFile(Global.unboundPath, unbound);
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





    public boolean restart() throws Exception {
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);
        // 重启 Unbound 服务
        Session session = conn.openSession();
        session.execCommand("systemctl restart unbound");
        Thread.sleep(1000);
        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        session = conn.openSession();
        session.execCommand("systemctl status unbound");
        String statusOutput = consumeInputStream(session.getStdout());
        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        boolean isRunning = checkUnboundStatus(conn);
        // 关闭连接
        conn.close();
        if (isRunning) {
            return true;
        } else {
            return false;
        }
    }

    private String consumeInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }


    private boolean checkUnboundStatus(Connection conn) throws Exception {
        Session session = conn.openSession();
        session.execCommand("systemctl status unbound");
        String statusOutput = consumeInputStream(session.getStdout());
        session.close(); // 关闭会话
        // 判断服务状态
        return statusOutput.contains("Active: active (running)");
    }



}
