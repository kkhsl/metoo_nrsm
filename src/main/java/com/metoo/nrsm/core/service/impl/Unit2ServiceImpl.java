package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.Unit2Mapper;
import com.metoo.nrsm.core.service.IUnit2Service;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class Unit2ServiceImpl implements IUnit2Service {

    @Resource
    private Unit2Mapper unit2Mapper;

    @Override
    public Unit2 selectObjById(Long id) {
        return this.unit2Mapper.selectObjById(id);
    }


    @Override
    public int update(Unit2 instance) {
        try {
            return this.unit2Mapper.update(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Result selectObjConditionQuery(Unit2 dto) {
        if(dto == null){
            dto = new Unit2();
        }
        List<Unit2> unit2s = this.unit2Mapper.selectObjConditionQuery(dto);
        return ResponseUtil.ok(unit2s);
    }
    @Override
    public Result selectAllQuery() {
        List<Unit2> units = this.unit2Mapper.selectAllQuery();
        return ResponseUtil.ok(units);
    }

    @Override
    public Result save(Unit2 instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setAddTime(new Date());
            int i = this.unit2Mapper.save(instance);
            if (i >= 1) {
                return ResponseUtil.ok();
            }
        } else {
            int i = this.unit2Mapper.update(instance);
            instance.setUpdateTime(new Date());
            if (i >= 1) {
                return ResponseUtil.ok();
            }
        }
        return ResponseUtil.saveError();
    }

    @Override
    public Result delete(String ids) {
        if(ids != null && !ids.equals("")){
            for (String id : ids.split(",")){
                Unit2 unit = this.unit2Mapper.selectObjById(Long.parseLong(id));
                if(unit != null){
                    try {
                        this.unit2Mapper.delete(Long.parseLong(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return ResponseUtil.badArgument(unit.getUnitName() + "删除失败");
                    }
                }
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }

}
