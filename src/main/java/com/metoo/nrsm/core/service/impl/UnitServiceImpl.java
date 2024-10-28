package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.GatewayDTO;
import com.metoo.nrsm.core.dto.UnitDTO;
import com.metoo.nrsm.core.mapper.UnitMapper;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.service.IVendorService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.Unit;
import com.metoo.nrsm.entity.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional
public class UnitServiceImpl implements IUnitService {

    @Resource
    private UnitMapper unitMapper;
    @Autowired
    private IGatewayService gatewayService;
    @Autowired
    private IVendorService vendorService;

    @Override
    public Unit selectObjById(Long id) {
        return this.unitMapper.selectObjById(id);
    }

    @Override
    public List<Unit> selectObjByMap(Map params) {
        return this.unitMapper.selectObjByMap(params);
    }

    @Override
    public List<Unit> selectObjByMapToMonitor(Map params) {
        return this.unitMapper.selectObjByMapToMonitor(params);
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
    public Result selectObjConditionQuery(UnitDTO dto) {
        if(dto == null){
            dto = new UnitDTO();
        }
        Page<Unit> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.unitMapper.selectObjConditionQuery(dto);

        if (page.getResult().size() > 0) {
            for (Unit instance : page.getResult()) {
                if(instance.getGatewayId() != null
                        && !instance.getGatewayId().equals("")){
                    Gateway gateway = this.gatewayService.selectObjById(instance.getGatewayId());
                    if(gateway != null){
                        instance.setGatewayName(gateway.getName());
                    }
                }
            }
        }

        List<Gateway> gatewayList = this.gatewayService.selectObjByMap(null);
        Map data = new HashMap();
        data.put("gateway", gatewayList);

        return ResponseUtil.ok(new PageInfo<Unit>(page, data));
    }

    @Override
    public Result add() {
        Map data = new HashMap();
        List<Gateway> gatewayList = this.gatewayService.selectObjByMap(null);
        if(gatewayList.size() > 0){
            for (Gateway gateway : gatewayList) {
                if(gateway.getVendorId() != null && !gateway.getVendorId().equals("")){
                    Vendor vendor = this.vendorService.selectObjById(gateway.getVendorId());
                    if(vendor != null){
                        gateway.setVendorName(vendor.getName());
                        gateway.setVendorAlias(vendor.getNameEn());
                    }
                }

            }
        }
        data.put("gateway", gatewayList);
        return ResponseUtil.ok(data);
    }

    @Override
    public Result save(Unit instance) {
        if(instance.getGatewayId() != null && !instance.getGatewayId().equals("")){
            Gateway gateway = this.gatewayService.selectObjById(instance.getGatewayId());
            if(gateway == null){
                return ResponseUtil.badArgument("网关设备不存在");
            }
        }
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setAddTime(new Date());
            int i = this.unitMapper.save(instance);
            if (i >= 1) {
                return ResponseUtil.ok();
            }
        } else {
            int i = this.unitMapper.update(instance);
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
