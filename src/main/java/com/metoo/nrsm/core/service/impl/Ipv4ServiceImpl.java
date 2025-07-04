package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.Ipv4Mapper;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.entity.Ipv4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:31
 */
@Service
@Transactional/*(rollbackFor = Exception.class)*/
public class Ipv4ServiceImpl implements Ipv4Service {

    @Autowired
    private Ipv4Mapper ipv4Mapper;

    @Override
    public List<Ipv4> selectDuplicatesObjByMap(Map params) {
        return this.ipv4Mapper.selectDuplicatesObjByMap(params);
    }

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
     * 避免清表和写入之间存在空表时间
     * <p>
     * 将清表和写入放到同一事务中，
     */
    @Override
    public boolean clearAndcopyGatherDataToIpv4() {
//        try {
//
////            this.ipv4Mapper.truncateTable();// （TRUNCATE TABLE操作更加高效但无法被事务管理所控制，改用delete）
//            this.ipv4Mapper.deleteTable();
//
//            Thread.sleep(10000);
//            int i = 1 / 0;
//
//            this.ipv4Mapper.copyGatherToIpv4();
//
//            return true;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            // 如果清空表或插入数据过程中发生异常，则抛出 RuntimeException
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            return false;
//        }

        this.ipv4Mapper.deleteTable();

        this.ipv4Mapper.copyGatherToIpv4();

//        int i = 1 / 0;

        return true;
    }


    // 确保删除和写入在同一个事务中，避免出现空表情况
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
