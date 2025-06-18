package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.mapper.FlowUnitMapper;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.IVendorService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.Unit;
import com.metoo.nrsm.entity.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FlowUnitServiceImpl implements IFlowUnitService {

    @Resource
    private FlowUnitMapper flowUnitMapper;
    @Autowired
    private IGatewayService gatewayService;
    @Autowired
    private IVendorService vendorService;
    @Autowired
    private IUnitService unitService;

    @Override
    public FlowUnit selectObjById(Long id) {
        return this.flowUnitMapper.selectObjById(id);
    }

    @Override
    public List<FlowUnit> selectObjByMap(Map params) {
        return this.flowUnitMapper.selectObjByMap(params);
    }
    @Override
    public List<FlowUnit> selectByUnitId(Long unitId) {
        return this.flowUnitMapper.selectByUnitId(unitId);
    }

    @Override
    public List<FlowUnit> selectObjByMapToMonitor(Map params) {
        return this.flowUnitMapper.selectObjByMapToMonitor(params);
    }

    @Override
    public int update(FlowUnit instance) {
        try {
            return this.flowUnitMapper.update(instance);
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
        Page<FlowUnit> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.flowUnitMapper.selectObjConditionQuery(dto);

        if (page.getResult().size() > 0) {
            for (FlowUnit instance : page.getResult()) {
                if(instance.getGatewayId() != null
                        && !instance.getGatewayId().equals("")){
                    Gateway gateway = this.gatewayService.selectObjById(instance.getGatewayId());
                    if(gateway != null){
                        instance.setGatewayName(gateway.getName());
                    }
                }
                if(instance.getUnitId() != null && !instance.getUnitId().equals("")){
                    Unit unit2 = this.unitService.selectObjById(instance.getUnitId());
                    if(unit2 != null){
                        instance.setUnitName(unit2.getUnitName());
                    }
                }
            }
        }

        List<Gateway> gatewayList = this.gatewayService.selectObjByMap(null);
        Map data = new HashMap();
        data.put("gateway", gatewayList);
        return ResponseUtil.ok(new PageInfo<FlowUnit>(page, data));
    }
    @Override
    public Result selectAllQuery() {
        List<FlowUnit> units = this.flowUnitMapper.selectAllQuery();
        return ResponseUtil.ok(units);
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
        List<Unit> unit2s = this.unitService.selectUnitAll();
        data.put("unitList", unit2s);
        return ResponseUtil.ok(data);
    }

    @Override
    public Result save(FlowUnit instance) {
        if(instance.getGatewayId() != null && !instance.getGatewayId().equals("")){
            Gateway gateway = this.gatewayService.selectObjById(instance.getGatewayId());
            if(gateway == null){
                return ResponseUtil.badArgument("网关设备不存在");
            }
        }
        if(instance.getUnitId() != null && !instance.getUnitId().equals("")){
            Unit unit2 = this.unitService.selectObjById(instance.getUnitId());
            if(unit2 == null){
                return ResponseUtil.badArgument("单位不存在");
            }else{
                instance.setUnitName(unit2.getUnitName());
            }
        }
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setAddTime(new Date());
            int i = this.flowUnitMapper.save(instance);
            if (i >= 1) {
                return ResponseUtil.ok();
            }
        } else {
            int i = this.flowUnitMapper.update(instance);
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
                FlowUnit unit = this.flowUnitMapper.selectObjById(Long.parseLong(id));
                if(unit != null){
                    try {
                        this.flowUnitMapper.delete(Long.parseLong(id));
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
