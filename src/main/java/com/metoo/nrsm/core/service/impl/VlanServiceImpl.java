package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.VlanDTO;
import com.metoo.nrsm.core.mapper.VlanMapper;
import com.metoo.nrsm.core.service.IVlanService;
import com.metoo.nrsm.entity.Vlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class VlanServiceImpl implements IVlanService {

    @Autowired
    private VlanMapper vlanMapper;

    @Override
    public Vlan selectObjById(Long id) {
        Vlan vlan = this.vlanMapper.selectObjById(id);
        return vlan;
    }

    @Override
    public Page<Vlan> selectObjConditionQuery(VlanDTO dto) {
        Page<Vlan> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.vlanMapper.selectObjConditionQuery(dto);
        return page;
    }

    @Override
    public List<Vlan> selectObjByMap(Map params) {
        return this.vlanMapper.selectObjByMap(params);
    }

    @Override
    public int save(Vlan instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setAddTime(new Date());
            instance.setEditDate(new Date());
        }
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                return this.vlanMapper.save(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            try {
                instance.setEditDate(new Date());
                return this.vlanMapper.update(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    public int update(Vlan instance) {
        try {
            instance.setEditDate(new Date());
            return this.vlanMapper.update(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            return this.vlanMapper.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
