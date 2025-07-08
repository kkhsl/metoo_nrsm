package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.Dhcp6Dto;
import com.metoo.nrsm.core.mapper.Dhcp6HistoryMapper;
import com.metoo.nrsm.core.service.IDhcp6HistoryService;
import com.metoo.nrsm.entity.Dhcp6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-16 11:14
 */
@Service
@Transactional
public class Dhcp6HistoryServiceImpl implements IDhcp6HistoryService {

    @Autowired
    private Dhcp6HistoryMapper dhcp6HistoryMapper;

    @Override
    public Dhcp6 selectObjById(Long id) {
        return this.dhcp6HistoryMapper.selectObjById(id);
    }

    @Override
    public Dhcp6 selectObjByLease(String lease) {
        return this.dhcp6HistoryMapper.selectObjByLease(lease);
    }

    @Override
    public Page<Dhcp6> selectConditionQuery(Dhcp6Dto dto) {
        if (dto == null) {
            dto = new Dhcp6Dto();
        }
        Page<Dhcp6> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.dhcp6HistoryMapper.selectConditionQuery(dto);
        return page;
    }

    @Override
    public List<Dhcp6> selectObjByMap(Map params) {
        return this.dhcp6HistoryMapper.selectObjByMap(params);
    }

    @Override
    public boolean save(Dhcp6 instance) {
        if (instance.getId() == null) {
            try {
                this.dhcp6HistoryMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                this.dhcp6HistoryMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean update(Dhcp6 instance) {
        try {
            this.dhcp6HistoryMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            this.dhcp6HistoryMapper.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int truncateTable() {
        return this.dhcp6HistoryMapper.truncateTable();
    }

    @Override
    public int batchInsert() {
        return this.dhcp6HistoryMapper.batchInsert();
    }

}
