package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.DomainDTO;
import com.metoo.nrsm.core.mapper.DomainMapper;
import com.metoo.nrsm.core.service.IDomainService;
import com.metoo.nrsm.entity.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DomainServiceImpl implements IDomainService {

    @Autowired
    private DomainMapper domainMapper;

    @Override
    public Domain selectObjById(Long id) {
        Domain domain = this.domainMapper.selectObjById(id);
        return domain;
    }

    @Override
    public Page<Domain> selectObjConditionQuery(DomainDTO dto) {
        Page<Domain> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.domainMapper.selectObjConditionQuery(dto);
        return page;
    }

    @Override
    public List<Domain> selectObjByMap(Map params) {
        return this.domainMapper.selectObjByMap(params);
    }

    @Override
    public List<Domain> selectDomainAndVlanByMap(Map params) {
        return this.domainMapper.selectDomainAndVlanByMap(params);
    }

    @Override
    public List<Domain> selectDomainAndVlanProceDureByMap(Map params) {
        return this.domainMapper.selectDomainAndVlanProceDureByMap(params);
    }

    @Override
    public int save(Domain instance) {
        if(instance.getId() == null || instance.getId().equals("")){
            instance.setAddTime(new Date());
            instance.setEditDate(new Date());
        }
        if(instance.getId() == null || instance.getId().equals("")){
            try {
                return this.domainMapper.save(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {
                instance.setEditDate(new Date());
                return this.domainMapper.update(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    public int update(Domain instance) {
        try {
            return this.domainMapper.update(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            return this.domainMapper.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
