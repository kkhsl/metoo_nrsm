package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.PortIpv6Mapper;
import com.metoo.nrsm.core.mapper.PortMapper;
import com.metoo.nrsm.core.service.IPortIpv6Service;
import com.metoo.nrsm.core.service.IPortService;
import com.metoo.nrsm.entity.Port;
import com.metoo.nrsm.entity.PortIpv6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-18 10:36
 */

@Service
@Transactional
public class PortIpv6ServiceImpl implements IPortIpv6Service {

    @Autowired
    private PortIpv6Mapper portIpv6Mapper;

    @Override
    public List selectObjByMap(Map params) {
        return this.portIpv6Mapper.selectObjByMap(params);
    }

    @Override
    public List selectObjByDeviceUuid(String deviceUuid) {
        return this.portIpv6Mapper.selectObjByDeviceUuid(deviceUuid);
    }

    @Override
    public boolean save(PortIpv6 instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.portIpv6Mapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        } else {
            try {
                this.portIpv6Mapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }
    }

    @Override
    public boolean update(PortIpv6 instance) {
        try {
            this.portIpv6Mapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean batchSave(PortIpv6 instance) {
        try {
            this.portIpv6Mapper.batchSave(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean batchSaveGather(List<PortIpv6> instance) {
        try {
            this.portIpv6Mapper.batchSaveGather(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean truncateTableGather() {
        try {
            this.portIpv6Mapper.truncateTableGather();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean deleteTable() {
        try {
            this.portIpv6Mapper.deleteTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean copyGatherDataToPortIpv6() {
        try {
            this.portIpv6Mapper.copyGatherDataToPortIpv6();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean copyGatherData() {
        try {

            int i = this.portIpv6Mapper.deleteTable();

            int ii = this.portIpv6Mapper.copyGatherDataToPortIpv6();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    @Override
    public List<PortIpv6> selctVlanNumberBySplitFieldFunction() {
        return this.portIpv6Mapper.selctVlanNumberBySplitFieldFunction();
    }

    @Override
    public List<PortIpv6> selctVlanNumberByREGEXPREPLACE() {
        return this.portIpv6Mapper.selctVlanNumberByREGEXPREPLACE();
    }
}
