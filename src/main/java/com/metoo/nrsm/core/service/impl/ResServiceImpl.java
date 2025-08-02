package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.ResDto;
import com.metoo.nrsm.core.mapper.ResMapper;
import com.metoo.nrsm.core.service.IResService;
import com.metoo.nrsm.entity.Res;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("resService")
@Transactional
public class ResServiceImpl implements IResService {

    @Autowired
    private ResMapper resMapper;

    @Override
    public List<Res> findResByRoleId(Long id) {
        return resMapper.findResByRoleId(id);
    }

    @Override
    public Res findObjById(Long id) {
        return resMapper.selectPrimaryById(id);
    }

    @Override
    public Res findObjByName(String name) {
        return resMapper.findObjByName(name);
    }

    @Override
    public Res findObjByNameAndLevel(Map map) {
        return resMapper.findObjByNameAndLevel(map);
    }

    @Override
    public Res findResUnitRoleByResId(Long id) {
        return resMapper.findResUnitRoleByResId(id);
    }

    @Override
    public Page<Res> query(ResDto dto) {
        Page<Res> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        resMapper.query();
        return page;
    }

    @Override
    public List<Res> findPermissionByJoin(Map map) {
        return resMapper.findPermissionByJoin(map);
    }

    @Override
    public List<Res> findPermissionByMap(Map map) {
        return resMapper.findPermissionByMap(map);
    }

    @Override
    public List<Res> findResByResIds(List<Integer> ids) {
        return resMapper.findResByResIds(ids);
    }

    @Override
    public List<Res> selectObjByMap(Map params) {
        return resMapper.selectObjByMap(params);
    }

    @Override
    public Collection<String> findPermissionByUserId(Long id) {
        return resMapper.findPermissionByUserId(id);
    }

    @Override
    public boolean save(ResDto dto) {
        Res obj = null;
        if (dto.getId() == null) {
            obj = new Res();
            obj.setAddTime(new Date());
        } else {
            obj = resMapper.selectPrimaryById(dto.getId());
        }
        if (obj != null) {
            BeanUtils.copyProperties(dto, obj);
            Res res = resMapper.selectPrimaryById(dto.getParentId());
            if (res != null) {
                obj.setParentId(res.getId());
                obj.setParentName(res.getName());
            }
            obj.setType("URL");
            if (obj.getId() == null) {
                try {
                    resMapper.save(obj);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                try {
                    resMapper.update(obj);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean update(Res instance) {
        try {
            resMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean delete(Long id) {
        try {
            resMapper.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
