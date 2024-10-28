package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.DhcpDto;
import com.metoo.nrsm.core.mapper.DhcpHistoryMapper;
import com.metoo.nrsm.core.service.IDhcpHistoryService;
import com.metoo.nrsm.entity.Dhcp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-16 11:14
 */
@Service
@Transactional
public class DhcpHistoryServiceImpl implements IDhcpHistoryService {

    @Autowired
    private DhcpHistoryMapper dhcpHistoryMapper;

    @Override
    public Dhcp selectObjById(Long id) {
        return this.dhcpHistoryMapper.selectObjById(id);
    }

    @Override
    public Dhcp selectObjByLease(String lease) {
        return this.dhcpHistoryMapper.selectObjByLease(lease);
    }

    @Override
    public Page<Dhcp> selectConditionQuery(DhcpDto dto) {
        if(dto == null){
            dto = new DhcpDto();
        }
        Page<Dhcp> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.dhcpHistoryMapper.selectConditionQuery(dto);
        return page;
    }

    @Override
    public List<Dhcp> selectObjByMap(Map params) {
        return this.dhcpHistoryMapper.selectObjByMap(params);
    }

    @Override
    public boolean save(Dhcp instance) {
        if(instance.getId() == null){
            try {
                this.dhcpHistoryMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }else{
            try {
                this.dhcpHistoryMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean update(Dhcp instance) {
        try {
            this.dhcpHistoryMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            this.dhcpHistoryMapper.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int truncateTable() {
        return this.dhcpHistoryMapper.truncateTable();
    }

    @Override
    public int batchInsert() {
        return this.dhcpHistoryMapper.batchInsert();
    }

}
