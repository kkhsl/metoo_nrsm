package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.PortMapper;
import com.metoo.nrsm.core.service.IPortService;
import com.metoo.nrsm.core.utils.collections.ListSortUtil;
import com.metoo.nrsm.entity.Port;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-18 10:36
 */

@Service
@Transactional
public class PortServiceImpl implements IPortService {

    @Autowired
    private PortMapper portMapper;

    @Override
    public List selectObjByMap(Map params) {
        return this.portMapper.selectObjByMap(params);
    }

    @Override
    public List selectObjByDeviceUuid(String deviceUuid) {
        List<Port> ports = this.portMapper.selectObjByDeviceUuid(deviceUuid);
        Collections.sort(ports, (o1, o2) -> ListSortUtil.compareString(o1.getPort(), o2.getPort()));
        return ports;
    }

    @Override
    public boolean save(Port instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.portMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }else{
            try {
                this.portMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }
    }

    @Override
    public boolean update(Port instance) {
        try {
            this.portMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean batchSave(Port instance) {
        try {
            this.portMapper.batchSave(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean batchSaveGather(List<Port> instance) {
        try {
            this.portMapper.batchSaveGather(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean truncateTableGather() {
        try {
            this.portMapper.truncateTableGather();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean deleteTable() {
        try {
            this.portMapper.deleteTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean copyGatherDataToPort() {
        try {
            this.portMapper.copyGatherDataToPort();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean copyGatherData() {
        try {

            int i = this.portMapper.deleteTable();

            int ii = this.portMapper.copyGatherDataToPort();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    @Override
    public List<Port> selctVlanNumberBySplitFieldFunction() {
        return this.portMapper.selctVlanNumberBySplitFieldFunction();
    }

    @Override
    public List<Port> selctVlanNumberByREGEXPREPLACE() {
        return this.portMapper.selctVlanNumberByREGEXPREPLACE();
    }
}
