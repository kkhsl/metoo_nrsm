package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.TerminalAssetDTO;
import com.metoo.nrsm.core.manager.utils.MacUtils;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.IProjectService;
import com.metoo.nrsm.core.service.ITerminalAssetService;
import com.metoo.nrsm.core.service.IVendorService;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/terminal/asset")
public class TerminalAssetManagerController {

    @Autowired
    private ITerminalAssetService terminalService;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private IVendorService vendorService;
    @Autowired
    private IProjectService projectService;
    @Autowired
    private MacUtils macUtils;

    @PostMapping("/list")
    public Object list(@RequestBody TerminalAssetDTO dto) {
        if (dto.getStart_purchase_time() != null && dto.getEnd_purchase_time() != null) {
            if (dto.getStart_purchase_time().after(dto.getEnd_purchase_time())) {
                return ResponseUtil.badArgument("起始时间需要小于结束时间");
            }
        }
        if (dto.getStart_warranty_time() != null && dto.getEnd_warranty_time() != null) {
            if (dto.getStart_warranty_time().after(dto.getEnd_warranty_time())) {
                return ResponseUtil.badArgument("起始时间需要小于结束时间");
            }
        }

        Page<TerminalAsset> page = this.terminalService.selectObjByConditionQuery(dto);

        if (page.size() > 0) {
            page.getResult().stream().forEach(e -> {
                if (e.getDeviceTypeId() != null && !e.getDeviceTypeId().equals("")) {
                    DeviceType deviceType = this.deviceTypeService.selectObjById(e.getDeviceTypeId());
                    if (deviceType != null) {
                        e.setDeviceTypeName(deviceType.getName());
                    }
                }
                if (e.getVendorId() != null && !e.getVendorId().equals("")) {
                    Vendor vendor = this.vendorService.selectObjById(e.getVendorId());
                    if (vendor != null) {
                        e.setVendorName(vendor.getName());
                    }
                }

                if (e.getProjectId() != null && !e.getProjectId().equals("")) {
                    Project project = this.projectService.selectObjById(e.getProjectId());
                    if (project != null) {
                        e.setProjectName(project.getName());
                    }
                }
                macUtils.terminalAssetSetMacVendor(e);
            });
        }
        Map map = new HashMap();
        // 设备类型
        Map params = new HashMap();
        params.put("diff", 1);
        params.put("orderBy", "sequence");
        params.put("orderType", "DESC");
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(params);
        map.put("deviceTypeList", deviceTypeList);
        // 品牌
        List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
        map.put("vendor", vendors);
        // 项目

        User user = ShiroUserHolder.currentUser();
        params.clear();
        params.put("userId", user.getId());
        List<Project> projectList = this.projectService.selectObjByMap(params);
        map.put("project", projectList);

        return ResponseUtil.ok(new PageInfo<TerminalAsset>(page, map));
    }

    @GetMapping("/add")
    public Object add() {
        // 设备类型
        Map map = new HashMap();
        User user = ShiroUserHolder.currentUser();
        Map parmas = new HashMap();
        parmas.put("diff", 1);
        parmas.put("orderBy", "sequence");
        parmas.put("orderType", "DESC");
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
        map.put("deviceTypeList", deviceTypeList);
        // 厂商
        List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
        map.put("vendor", vendors);
        // 项目
        Map params = new HashMap();
        params.put("userId", user.getId());
        List<Project> projectList = this.projectService.selectObjByMap(params);
        map.put("project", projectList);

        return ResponseUtil.ok(map);
    }

    @GetMapping("/update/{id}")
    public Object update(@PathVariable Long id) {
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        TerminalAsset terminal = this.terminalService.selectObjById(id);
        if (terminal == null) {
            return ResponseUtil.badArgument();
        } else {
            // 设备类型
            if (terminal.getDeviceTypeId() != null) {
                DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                if (deviceType != null) {
                    terminal.setDeviceTypeName(deviceType.getName());
                }
            }
        }
        Map map = new HashMap();
        map.put("terminal", terminal);

        User user = ShiroUserHolder.currentUser();
        // 设备类型
        Map parmas = new HashMap();
        parmas.put("diff", 1);
        parmas.put("orderBy", "sequence");
        parmas.put("orderType", "DESC");
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
        map.put("deviceTypeList", deviceTypeList);

        // 厂商
        List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
        map.put("vendor", vendors);
        // 项目
        Map params = new HashMap();
        params.put("userId", user.getId());
        List<Project> projectList = this.projectService.selectObjByMap(params);
        map.put("project", projectList);

        return ResponseUtil.ok(map);
    }

    @GetMapping("/verify")
    public Object verifyIp(@RequestParam(value = "id", required = false) Long id,
                           @RequestParam(value = "v4ip", required = true) String v4ip) {
        // 校验Ip
        if (!StringUtils.isEmpty(v4ip)) {
            boolean flag = Ipv4Util.verifyIp(v4ip);
            if (flag) {
                Map params = new HashMap();
                params.clear();
                params.put("v4ip", v4ip);
                params.put("terminalId", id);
                List<TerminalAsset> terminals = this.terminalService.selectObjByMap(params);
                if (terminals.size() > 0) {
                    return ResponseUtil.badArgument("Ip已存在");
                }
                return ResponseUtil.ok();
            } else {
                return ResponseUtil.badArgument("Ip格式错误");
            }
        }
        return ResponseUtil.badArgument("Ip为空");
    }

    @PostMapping("/save")
    public Object save(@RequestBody TerminalAsset instance) {
        // 验证名称是否唯一
        Map params = new HashMap();
        if (instance.getId() != null && !instance.getId().equals("")) {
            TerminalAsset terminal = this.terminalService.selectObjById(instance.getId());
            if (terminal == null) {
                return ResponseUtil.badArgument("终端不存在");
            }
        }
        // 验证Ip唯一性
        if (instance.getV4ip() == null || instance.getV4ip().equals("")) {
            return ResponseUtil.badArgument("请输入有效IP");
        } else {
            // 验证ip合法性
            boolean flag = Ipv4Util.verifyIp(instance.getV4ip());
            if (!flag) {
                return ResponseUtil.badArgument("ip不合法");
            }
            params.clear();
            params.put("v4ip", instance.getV4ip());
            params.put("terminalId", instance.getId());
            List<TerminalAsset> terminals = this.terminalService.selectObjByMap(params);
            if (terminals.size() > 0) {
                return ResponseUtil.badArgument("IP重复");
            }
        }

        // 验证资产编号唯一性
        if (instance.getAsset_number() != null && !instance.getAsset_number().isEmpty()) {
            params.clear();
            params.put("asset_number", instance.getAsset_number());
            params.put("terminalId", instance.getId());
            List<TerminalAsset> terminals = this.terminalService.selectObjByMap(params);
            if (terminals.size() > 0) {
                TerminalAsset terminal = terminals.get(0);
                return ResponseUtil.badArgument("资产编号与(" + terminal.getName() + ")设备重复");
            }
        }

        // 验证日期
        if (instance.getWarranty_time() != null && instance.getPurchase_time() != null) {
            if (instance.getWarranty_time().before(instance.getPurchase_time())) {
                return ResponseUtil.badArgument("过保时间必须大于采购时间");
            }
        }

        // 验证厂商
        if (instance.getVendorId() != null && !instance.getVendorId().equals("")) {
            Vendor vendor = this.vendorService.selectObjById(instance.getVendorId());
            if (vendor == null) {
                return ResponseUtil.badArgument("请输入正确品牌参数");
            }
        }

        // 验证项目
        if (instance.getProjectId() != null && !instance.getProjectId().equals("")) {
            Project project = this.projectService.selectObjById(instance.getProjectId());
            if (project == null) {
                return ResponseUtil.badArgument("请输入正确项目参数");
            }
        }

        // 设备类型
        if (instance.getDeviceTypeId() != null) {
            DeviceType deviceType = this.deviceTypeService.selectObjById(instance.getDeviceTypeId());
            if (deviceType == null) {
                return ResponseUtil.badArgument("请选择设备类型");
            } else {
                if (Strings.isBlank(instance.getName())) {
                    instance.setName(deviceType.getName());
                }
            }
        }
        if (instance.getUuid() != null && !instance.getUuid().equals("")) {
            instance.setFrom(3);
            instance.setInterfaceStatus(1);
        }
        if (instance.getInterfaceName() != null && !instance.getInterfaceName().equals("")) {
            instance.setIndex(instance.getInterfaceName().replace("Port", ""));
        }
        boolean flag = this.terminalService.save(instance);
        if (flag) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @DeleteMapping
    public Object delete(Long id, String ids) {
        if (Strings.isNotBlank(ids) && ids.split(",").length > 0) {
            String[] idList = ids.split(",");
            for (String s : idList) {
                int i = this.terminalService.delete(Long.parseLong(s));
            }
            return ResponseUtil.ok();
        } else {
            TerminalAsset terminal = this.terminalService.selectObjById(id);
            if (terminal != null) {
                int i = this.terminalService.delete(id);
                if (i >= 1) {
                    return ResponseUtil.ok();
                } else {
                    return ResponseUtil.error();
                }
            }
        }
        return ResponseUtil.badArgument();
    }

}
