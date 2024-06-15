package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.PlantRoomDTO;
import com.metoo.nrsm.core.mapper.PlantRoomMapper;
import com.metoo.nrsm.core.mapper.UserMapper;
import com.metoo.nrsm.core.service.IPlantRoomService;
import com.metoo.nrsm.core.vo.PlantRoomVO;
import com.metoo.nrsm.entity.PlantRoom;
import com.metoo.nrsm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PlantRoomServiceImpl implements IPlantRoomService {

    @Autowired
    private PlantRoomMapper plantRoomMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PlantRoom getObjById(Long id) {
        return this.plantRoomMapper.getObjById(id);
    }

    @Override
    public PlantRoom selectObjByName(String name) {
        return this.plantRoomMapper.selectObjByName(name);
    }

    @Override
    public List<PlantRoomVO> query(PlantRoom instance) {
        if(instance == null){
            instance = new PlantRoom();
        }
//        User user = ShiroUserHolder.currentUser();
//        instance.setUserId(user.getId());
        return this.plantRoomMapper.query(instance);
    }

    @Override
    public Page<PlantRoom> selectConditionQuery(PlantRoomDTO instance) {
//        User user = ShiroUserHolder.currentUser();
//        instance.setUserId(user.getId());
        Page<PlantRoom> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.plantRoomMapper.selectConditionQuery(instance);
        return page;
    }

    @Override
    public Page<PlantRoom> findBySelectAndRack(PlantRoomDTO instance) {
//        User user = ShiroUserHolder.currentUser();
//        instance.setUserId(user.getId());
        Page<PlantRoom> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.plantRoomMapper.findBySelectAndRack(instance);
        return page;
    }

    @Override
    public List<PlantRoom> selectObjByCard() {
        Map params = new HashMap();
//        User user = ShiroUserHolder.currentUser();
//        params.put("userId", user.getId());
        return this.plantRoomMapper.selectObjByCard(params);
    }

    @Override
    public List<PlantRoom> selectObjByMap(Map params) {
//        User user = ShiroUserHolder.currentUser();
//        params.put("userId", user.getId());
        return this.plantRoomMapper.selectObjByMap(params);
    }


    @Override
    public List<PlantRoomVO> selectVoByMap(Map params) {
//        User user = ShiroUserHolder.currentUser();
//        params.put("userId", user.getId());
        return this.plantRoomMapper.selectVoByMap(params);
    }

    @Override
    public int save(PlantRoom instance) {
        if(instance.getId() == null){
            instance.setAddTime(new Date());
//            if(instance.getUserId() == null){
//                User user = ShiroUserHolder.currentUser();
//                instance.setUserId(user.getId());
//            }
            return this.plantRoomMapper.save(instance);
        }else{
            return this.plantRoomMapper.update(instance);
        }
    }

    @Override
    public int delete(Long id) {
        return this.plantRoomMapper.delete(id);
    }

    @Override
    public int batchDel(String ids) {
        return this.plantRoomMapper.batchDel(ids);
    }
}
