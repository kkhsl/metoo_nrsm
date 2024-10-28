package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.Ipv4DetailMapper;
import com.metoo.nrsm.core.service.Ipv4DetailService;
import com.metoo.nrsm.entity.Ipv4Detail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Service
@Transactional
public class IpV4DetailServiceImpl implements Ipv4DetailService {

    @Autowired
    private Ipv4DetailMapper ipv4DetailMapper;

    @Override
    public List<Ipv4Detail> selectObjByMap(Map map) {
        return this.ipv4DetailMapper.selectObjByMap(map);
    }

    @Override
    public Ipv4Detail selectObjByMac(String mac) {
        return this.ipv4DetailMapper.selectObjByMac(mac);
    }

    @Override
    public Ipv4Detail selectObjByIp(String ip) {
        return this.ipv4DetailMapper.selectObjByIp(ip);
    }

    @Override
    public int save(Ipv4Detail instance) {
        if(instance.getId() == null){
            instance.setAddTime(new Date());
            return this.ipv4DetailMapper.save(instance);
        }else{
            return this.ipv4DetailMapper.update(instance);
        }
    }

    @Override
    public int update(Ipv4Detail instance) {
        return this.ipv4DetailMapper.update(instance);
    }
}
