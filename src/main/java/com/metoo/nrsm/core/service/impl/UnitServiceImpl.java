package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.mapper.UnitMapper;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UnitServiceImpl implements IUnitService {

    @Resource
    private UnitMapper unitMapper;

    @Override
    public Unit selectObjById(Long id) {
        return this.unitMapper.selectObjById(id);
    }


    @Override
    public int update(Unit instance) {
        try {
            return this.unitMapper.update(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Result selectObjConditionQuery(UnitNewDTO dto) {
        if(dto == null){
            dto = new UnitNewDTO();
        }
        Page<Unit> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.unitMapper.selectObjConditionQuery(dto);
        return ResponseUtil.ok(new PageInfo<Unit>(page));
    }

    @Override
    public Result selectAllQuery() {
        List<Unit> units = this.unitMapper.selectAllQuery();
        return ResponseUtil.ok(units);
    }

    @Override
    public List<Unit> selectUnitAll() {
        List<Unit> units = this.unitMapper.selectAllQuery();
        return units;
    }

    @Override
    public Result save(Unit instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setAddTime(new Date());
            int i = this.unitMapper.save(instance);
            if (i >= 1) {
                return ResponseUtil.ok();
            }
        } else {
            int i = this.unitMapper.update(instance);
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
                Unit unit = this.unitMapper.selectObjById(Long.parseLong(id));
                if(unit != null){
                    try {
                        this.unitMapper.delete(Long.parseLong(id));
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
