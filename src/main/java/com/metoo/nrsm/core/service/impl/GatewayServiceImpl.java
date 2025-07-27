package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.GatewayDTO;
import com.metoo.nrsm.core.manager.utils.AESUtils;
import com.metoo.nrsm.core.mapper.GatewayMapper;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.service.IVendorService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.Vendor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional
public class GatewayServiceImpl implements IGatewayService {

    @Resource
    private GatewayMapper gatewayMapper;
    @Autowired
    private IVendorService vendorService;

    @Override
    public Gateway selectObjById(Long id) {
        Gateway gateway = this.gatewayMapper.selectObjById(id);
        if (gateway.getVendorId() != null
                && !gateway.getVendorId().equals("")) {
            Vendor vendor = this.vendorService.selectObjById(gateway.getVendorId());
            if (vendor != null) {
                gateway.setVendorName(vendor.getName());
                gateway.setVendorAlias(vendor.getNameEn());
            }
        }
        return gateway;
    }

    @Override
    public List<Gateway> selectObjByMap(Map params) {

        List<Gateway> gateways = this.gatewayMapper.selectObjByMap(params);
        for (Gateway gateway : gateways) {
            if (gateway.getVendorId() != null
                    && !gateway.getVendorId().equals("")) {
                Vendor vendor = this.vendorService.selectObjById(gateway.getVendorId());
                if (vendor != null) {
                    gateway.setVendorName(vendor.getName());
                    gateway.setVendorAlias(vendor.getNameEn());
                }
            }
        }

        return gateways;
    }

    @Override
    public int update(Gateway instance) {
        try {
            return this.gatewayMapper.update(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Result selectObjConditionQuery(GatewayDTO dto) {
        if (dto == null) {
            dto = new GatewayDTO();
        }
        Page<Gateway> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.gatewayMapper.selectObjConditionQuery(dto);
        if (page.getResult().size() > 0) {
            for (Gateway instance : page.getResult()) {
                if (instance.getVendorId() != null
                        && !instance.getVendorId().equals("")) {
                    Vendor vendor = this.vendorService.selectObjById(instance.getVendorId());
                    if (vendor != null) {
                        instance.setVendorName(vendor.getName());
                        instance.setVendorAlias(vendor.getNameEn());
                    }
                }
            }
        }
        Map data = new HashMap();
        List<Vendor> deviceVendors = this.vendorService.selectObjByMap(null);
        data.put("deviceVendor", deviceVendors);
        return ResponseUtil.ok(new PageInfo<Gateway>(page, data));
    }


    @Override
    public Result save(Gateway instance) throws Exception {
        Result result = this.verifyParams(instance);
        if (result != null) {
            return this.verifyParams(instance);
        }
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setLoginPassword(AESUtils.encrypt(instance.getLoginPassword()));
            instance.setAddTime(new Date());
            UUID uuid = UUID.randomUUID();
            instance.setUuid(uuid.toString());
            int i = this.gatewayMapper.save(instance);
            if (i >= 1) {
                return ResponseUtil.ok();
            }
        } else {
            if (instance.getLoginPassword().equals(gatewayMapper.selectObjById(instance.getId()).getLoginPassword())){
                int i = this.gatewayMapper.update(instance);
                if (i >= 1) {
                    return ResponseUtil.ok();
                }
            }else {
                instance.setLoginPassword(AESUtils.encrypt(instance.getLoginPassword()));
                int i = this.gatewayMapper.update(instance);
                if (i >= 1) {
                    return ResponseUtil.ok();
                }
            }

        }
        return ResponseUtil.saveError();
    }

    @Override
    public Result delete(String ids) {
        if (ids != null && !ids.equals("")) {
            for (String id : ids.split(",")) {
                Gateway device = this.gatewayMapper.selectObjById(Long.parseLong(id));
                if (device != null) {
                    try {
                        this.gatewayMapper.delete(Long.parseLong(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return ResponseUtil.badArgument(device.getName() + "删除失败");
                    }
                }
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }

    @Override
    public Result batchSave(List<Gateway> devices) {
        // 校验id不为空对象是否修改
        for (Gateway device : devices) {
            Result result = this.verifyParams2(device);
            if (result != null) {
                return result;
            }
            if (device.getId() != null && !device.getId().equals("")) {
                Gateway obj = this.gatewayMapper.selectObjById(device.getId());
                if (obj == null) {
                    return ResponseUtil.badArgument(device.getName() + "参数错误");
                }
                // 对 JSON 字符串进行 MD5 加密
                String md5Value1 = DigestUtils.md5Hex(JSONObject.toJSONString(device));
                String md5Value2 = DigestUtils.md5Hex(JSONObject.toJSONString(obj));
                if (!md5Value1.equals(md5Value2)) {
                    try {
                        int i = this.gatewayMapper.update(device);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseUtil.error(device.getName() + "保存失败");
                    }
                }
            } else {
                UUID uuid = UUID.randomUUID();
                device.setUuid(uuid.toString());
                device.setAddTime(new Date());
                try {
                    int i = this.gatewayMapper.save(device);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseUtil.error(device.getName() + "保存失败");
                }
            }
        }
        return ResponseUtil.ok();
    }

    @Override
    public Result modify(Long id) {
        Gateway instance = null;
        if (id != null && !id.equals("")) {
            instance = this.gatewayMapper.selectObjById(id);
            if (instance != null) {
                if (instance.getVendorId() != null
                        && !instance.getVendorId().equals("")) {
                    Vendor vendor = this.vendorService.selectObjById(instance.getVendorId());
                    if (vendor != null) {
                        instance.setVendorName(vendor.getName());
                        instance.setVendorAlias(vendor.getNameEn());
                    }
                }
            } else {
                return ResponseUtil.badArgument("资源不存在");
            }
        }

        Map data = new HashMap();
        data.put("device", instance);
        List<Vendor> deviceVendors = this.vendorService.selectObjByMap(null);
        data.put("deviceVendor", deviceVendors);
        return ResponseUtil.ok(data);
    }

    public Result verifyParams(Gateway instance) {
        Map params = new HashMap();
        if (StringUtils.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument("设备名称不能为空");
        } else {
            params.clear();
            params.put("notId", instance.getId());
            params.put("name", instance.getName());
            List<Gateway> devices = this.gatewayMapper.selectObjByMap(params);
            if (devices.size() > 0) {
                return ResponseUtil.badArgument("设备名称不能重复");
            }
        }
        if (StringUtils.isEmpty(instance.getIp())) {
            return ResponseUtil.badArgument("设备Ip不能为空");
        } else {
            params.clear();
            params.put("notId", instance.getId());
            params.put("ip", instance.getIp());
            List<Gateway> devices = this.gatewayMapper.selectObjByMap(params);
            if (devices.size() > 0) {
                return ResponseUtil.badArgument("设备Ip不能重复");
            }
        }
        if (StringUtils.isEmpty(instance.getLoginName())) {
            return ResponseUtil.badArgument("登录名称不能为空");
        }
        if (StringUtils.isEmpty(instance.getLoginPassword())) {
            return ResponseUtil.badArgument("登录密码不能为空");
        }
        if (StringUtils.isEmpty(instance.getLoginPort())) {
            return ResponseUtil.badArgument("登录端口不能为空");
        }
        if (StringUtils.isEmpty(instance.getLoginType())) {
            return ResponseUtil.badArgument("登录类型不能为空");
        }
        if (instance.getVendorId() == null
                || instance.getVendorId().equals("")) {
            return ResponseUtil.badArgument("品牌不能为空");
        }
        return null;
    }

    public Result verifyParams2(Gateway instance) {
        Map params = new HashMap();
        if (StringUtils.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument(instance.getName() + "设备名称不能为空");
        } else {
            params.clear();
            params.put("notId", instance.getId());
            params.put("name", instance.getName());
            List<Gateway> devices = this.gatewayMapper.selectObjByMap(params);
            if (devices.size() > 0) {
                return ResponseUtil.badArgument(instance.getName() + "设备名称不能重复");
            }
        }
        if (StringUtils.isEmpty(instance.getIp())) {
            return ResponseUtil.badArgument(instance.getName() + "设备Ip不能为空");
        } else {
            params.clear();
            params.put("notId", instance.getId());
            params.put("ip", instance.getIp());
            List<Gateway> devices = this.gatewayMapper.selectObjByMap(params);
            if (devices.size() > 0) {
                return ResponseUtil.badArgument(instance.getName() + "设备Ip不能重复");
            }
        }
        if (StringUtils.isEmpty(instance.getLoginName())) {
            return ResponseUtil.badArgument(instance.getName() + "登录名称不能为空");
        }
        if (StringUtils.isEmpty(instance.getLoginPassword())) {
            return ResponseUtil.badArgument(instance.getName() + "登录密码不能为空");
        }
        if (StringUtils.isEmpty(instance.getLoginPort())) {
            return ResponseUtil.badArgument(instance.getName() + "登录端口不能为空");
        }
        if (StringUtils.isEmpty(instance.getLoginType())) {
            return ResponseUtil.badArgument(instance.getName() + "登录类型不能为空");
        }
        if (instance.getVendorId() == null
                || instance.getVendorId().equals("")) {
            return ResponseUtil.badArgument("品牌不能为空");
        }
        return null;
    }
}
