package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.mapper.AreaMapper;
import com.metoo.nrsm.core.mapper.UnitMapper;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Area;
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

    @Resource
    private AreaMapper areaMapper;

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
        if (dto == null) {
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
        instance.setCountyName(null);
        instance.setCityCode(null);
        if (instance.getCityName()==null){
            Area city = areaMapper.findByCode(instance.getCityCode());
            instance.setCityName(city.getName());
        }
        if (instance.getCountyName()==null){
            Area county = areaMapper.findByCode(instance.getCountyCode());
            instance.setCountyName(county.getName());
        }
        if (instance.getId() == null || instance.getId().equals("")) {
            // 检查unitName是否为空
            if (instance.getUnitName() == null || instance.getUnitName().isEmpty()) {
                return ResponseUtil.error("单位名称不能为空");
            }
            if (instance.getUnitId() == null || instance.getUnitId().isEmpty()) {
                return ResponseUtil.error("单位id不能为空");
            }
            // 根据unitName查询是否存在重复
            int count = this.unitMapper.countByUnitName(instance.getUnitName());
            List<Unit> units = unitMapper.selectByUnitName(instance.getUnitName());
            if (units.isEmpty()){
                // 新增逻辑
                instance.setAddTime(new Date());
                int i = this.unitMapper.save(instance);
                if (i >= 1) {
                    return ResponseUtil.ok();
                }
            }else {
                if (count > 0 && units.get(0).getDeleteStatus()==0) {
                    return ResponseUtil.error("单位名称已存在");
                }
                if (units.get(0).getDeleteStatus()==1){
                    instance.setDeleteStatus(0);
                    instance.setId(units.get(0).getId());
                    instance.setUpdateTime(new Date());
                    int i = this.unitMapper.update(instance);
                    if (i >= 1) {
                        return ResponseUtil.ok();
                    }
                }
            }

        } else {
            instance.setUpdateTime(new Date());
            int i = this.unitMapper.update(instance);
            if (i >= 1) {
                return ResponseUtil.ok();
            }
        }
        return ResponseUtil.saveError();
    }

    @Override
    public Result delete(String ids) {
        if (ids != null && !ids.equals("")) {
            for (String id : ids.split(",")) {
                Unit unit = this.unitMapper.selectObjById(Long.parseLong(id));
                if (unit != null) {
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
