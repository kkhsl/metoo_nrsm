package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.Ipv6Mapper;
import com.metoo.nrsm.core.service.Ipv6Service;
import com.metoo.nrsm.entity.Ipv6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2026-02-01 15:31
 */
@Service
@Transactional
public class Ipv6ServiceImpl implements Ipv6Service {

    @Autowired
    private Ipv6Mapper ipv6Mapper;

    @Override
    public boolean save(Ipv6 instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.ipv6Mapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }
        return false;
    }

    @Override
    public boolean saveGather(Ipv6 instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.ipv6Mapper.saveGather(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }
        return false;
    }

    @Override
    public boolean batchSaveGather(List<Ipv6> instance) {
        if (instance == null || instance.size() > 0) {
            try {
                this.ipv6Mapper.batchSaveGather(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }
        return false;
    }

    @Override
    public boolean truncateTableGather() {
        try {
            this.ipv6Mapper.truncateTableGather();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean copyGatherToIpv6() {
        try {
            this.ipv6Mapper.deleteTable();
            this.ipv6Mapper.copyGatherToIpv6();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;

        }
    }

    @Override
    public boolean removeDuplicates() {
        try {
            this.ipv6Mapper.removeDuplicates();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean truncateTable() {
        try {
            this.ipv6Mapper.truncateTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }
}
