package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.SubnetMapper;
import com.metoo.nrsm.core.service.ISubnetService;
import com.metoo.nrsm.core.utils.ip.IpV4Util;
import com.metoo.nrsm.entity.Subnet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SubnetServiceImpl implements ISubnetService {

    @Autowired
    private SubnetMapper subnetMapper;

    @Override
    public Subnet selectObjById(Long id) {
        return this.subnetMapper.selectObjById(id);
    }

    @Override
    public Subnet selectObjByIp(String ip) {
        return this.subnetMapper.selectObjByIp(ip);
    }

    @Override
    public Subnet selectObjByIpAndMask(String ip, Integer mask) {
        return this.subnetMapper.selectObjByIpAndMask(IpV4Util.ipConvertDec(ip), mask);
    }

    @Override
    public List<Subnet> selectSubnetByParentId(Long id) {
        return this.subnetMapper.selectSubnetByParentId(id);
    }

    @Override
    public List<Subnet> selectSubnetByParentIp(Long ip) {
        return this.subnetMapper.selectSubnetByParentIp(ip);
    }

    @Override
    public List<Subnet> selectObjByMap(Map params) {
        return this.subnetMapper.selectObjByMap(params);
    }

    @Override
    public int save(Subnet subnet) {
        try {
            subnet.setAddTime(new Date());
            return this.subnetMapper.save(subnet);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int update(Subnet subnet) {
        try {
            return this.subnetMapper.update(subnet);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            return this.subnetMapper.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
