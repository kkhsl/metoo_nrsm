package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.TrafficMapper;
import com.metoo.nrsm.core.service.ITrafficService;
import com.metoo.nrsm.entity.Traffic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TrafficServiceImpl implements ITrafficService {

    @Resource
    private TrafficMapper trafficMapper;

    @Override
    public Traffic selectObjById(Long id) {
        return this.trafficMapper.selectObjById(id);
    }

    @Override
    public List<Traffic> selectObjByMap(Map params) {
        return this.trafficMapper.selectObjByMap(params);
    }

    @Override
    public int save(Traffic instance) {
        if (instance.getAddTime() == null) {
            instance.setAddTime(new Date());
        }
        try {
            int i = this.trafficMapper.save(instance);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
