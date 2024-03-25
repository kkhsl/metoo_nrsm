package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.Ipv4Mapper;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.entity.Ipv4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:31
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class Ipv4ServiceImpl implements Ipv4Service {

    @Autowired
    private Ipv4Mapper ipv4Mapper;

    @Override
    public List<Ipv4> joinSelectObjAndIpv6() {
        return this.ipv4Mapper.joinSelectObjAndIpv6();
    }

    @Override
    public boolean save(Ipv4 instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.ipv4Mapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }
        return false;
    }

    @Override
    public boolean saveGather(Ipv4 instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.ipv4Mapper.saveGather(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }
        return false;
    }

    @Override
    public boolean batchSaveGather(List<Ipv4> instance) {
        if (instance == null || instance.size() > 0) {
            try {
                this.ipv4Mapper.batchSaveGather(instance);
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
            this.ipv4Mapper.truncateTableGather();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    /**
     * 避免出现，truncateTable后，出现空表情况下，arp读取ipv4
     * 解决方法：
     *      将ipv4数据的写入操作放到同一个事务中（清空表 + 写入最新数据）
     *      将ipv4写入、去重以及写入arp放到一个存储过程中（性能问题）
     *
     */
    @Override
    public boolean copyGatherToIpv4() {
        try {

//            this.ipv4Mapper.truncateTable();// （TRUNCATE TABLE操作更加高效但无法被ROLLBACK）
            // 改用delete，避免出现数据过多时，出现空表时长过久，放在同一个事务中
            this.ipv4Mapper.deleteTable();

//            Thread.sleep(10000);
//            int i = 1 / 0;

            this.ipv4Mapper.copyGatherToIpv4();

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
            this.ipv4Mapper.removeDuplicates();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean truncateTable() {
        try {
            this.ipv4Mapper.truncateTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public boolean deleteTable() {
        try {
            this.ipv4Mapper.deleteTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }
}
