package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.core.manager.utils.MacUtils;
import com.metoo.nrsm.core.mapper.TerminalMacIpv6Mapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/terminal")
public class TerminalManagerController {

    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private IVendorService vendorService;
    @Autowired
    private IProjectService projectService;
    @Autowired
    private MacUtils macUtils;
    @Autowired
    private ITerminalUnitService terminalUnitService;
    @Autowired
    private ITerminalMacIpv6Service terminalMacIpv6Service;
    @Autowired
    private TerminalMacIpv6Mapper terminalMacIpv6Mapper;

    @GetMapping("/vdt")
    public Result vdt(String ip){
        if(StringUtils.isNotEmpty(ip)){
            Map params = new HashMap();
            params.put("deviceIp", ip);
            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
            if(!terminals.isEmpty()){
                return ResponseUtil.ok(terminals);
            }
        }
      return ResponseUtil.ok();
    }

    @PostMapping("/list")
    public Object list(@RequestBody TerminalDTO dto){
        if(dto.getStart_purchase_time() != null && dto.getEnd_purchase_time() != null){
            if(dto.getStart_purchase_time().after(dto.getEnd_purchase_time())){
                return ResponseUtil.badArgument("起始时间需要小于结束时间");
            }
        }
        if(dto.getStart_warranty_time() != null && dto.getEnd_warranty_time() != null){
            if(dto.getStart_warranty_time().after(dto.getEnd_warranty_time())){
                return ResponseUtil.badArgument("起始时间需要小于结束时间");
            }
        }

        Page<Terminal> page = this.terminalService.selectObjByConditionQuery(dto);

        if(page.size() > 0){
            page.getResult().stream().forEach(e ->{
                if(e.getDeviceTypeId() != null && !e.getDeviceTypeId().equals("")){
                    DeviceType deviceType = this.deviceTypeService.selectObjById(e.getDeviceTypeId());
                    if(deviceType != null){
                        e.setDeviceTypeName(deviceType.getName());
                    }
                }
                if(e.getVendorId() != null && !e.getVendorId().equals("")){
                    Vendor vendor = this.vendorService.selectObjById(e.getVendorId());
                    if(vendor != null){
                        e.setVendorName(vendor.getName());
                    }
                }

                if(e.getProjectId() != null && !e.getProjectId().equals("")){
                    Project project = this.projectService.selectObjById(e.getProjectId());
                    if(project != null){
                        e.setProjectName(project.getName());
                    }
                }
                macUtils.terminalSetMacVendor(e);
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
        params.clear();
        List<Project> projectList = this.projectService.selectObjByMap(params);
        map.put("project", projectList);

        return ResponseUtil.ok(new PageInfo<Terminal>(page, map));
    }



    @GetMapping("/unit")
    public Result unitTerminal(){
        List<TerminalUnit> terminalUnitList = terminalUnitService.selectObjAndTerminalByMap(null);
        for (TerminalUnit terminalUnit : terminalUnitList) {
            macUtils.terminalJoint(terminalUnit.getTerminalList());
        }
        return ResponseUtil.ok(terminalUnitList);
    }

    @GetMapping("/ipv6")
    public void ipv6(){
        List<Terminal> terminalList = this.terminalService.selectObjByMap(null);
        if(terminalList.size() > 0){
            for (Terminal terminal : terminalList) {
                if(StringUtil.isNotEmpty(terminal.getV6ip())){
                    TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Mapper.getMacByMacAddress(terminal.getMac());
                    if(terminalMacIpv6 == null){
                        this.terminalMacIpv6Mapper.insertMac(terminal.getMac(), 1);
                    }
                }
            }
        }

    }

    @GetMapping("/unit/history")
    public Result unitTerminalHistory(){
        Map params = new HashMap();
        params.put("time", "2024-09-16 11:31:00");
        List<TerminalUnit> terminalUnitList = terminalUnitService.selectObjAndTerminalHistoryByMap(params);
        for (TerminalUnit terminalUnit : terminalUnitList) {
            macUtils.terminalJoint(terminalUnit.getTerminalList());
            if(terminalUnit.getTerminalList().size() > 0){
                for (Terminal terminal : terminalUnit.getTerminalList()) {
                    if(StringUtil.isNotEmpty(terminal.getMac())){
                        TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Service.getMacByMacAddress(terminal.getMac());
                        if(terminalMacIpv6 != null){
                            terminal.setIsIpv6(1);
                        }else{
                            terminal.setIsIpv6(0);
                        }
                    }
                }
            }
        }
        return ResponseUtil.ok(terminalUnitList);
    }

    @GetMapping("/add")
    public Object add(){
        // 设备类型
        Map map = new HashMap();
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
        List<Project> projectList = this.projectService.selectObjByMap(params);
        map.put("project", projectList);

        return ResponseUtil.ok(map);
    }

    @GetMapping("/update/{id}")
    public Object update(@PathVariable Long id){
        if(id == null){
            return  ResponseUtil.badArgument();
        }
        Terminal terminal = this.terminalService.selectObjById(id);
        if(terminal == null){
            return  ResponseUtil.badArgument();
        }else{
            // 设备类型
            if(terminal.getDeviceTypeId() != null){
                DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                if(deviceType != null){
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
        List<Project> projectList = this.projectService.selectObjByMap(params);
        map.put("project", projectList);

        return ResponseUtil.ok(map);
    }

    @GetMapping("/verify")
    public Object verifyIp(@RequestParam(value = "id", required = false) Long id,
                           @RequestParam(value = "v4ip", required = true) String v4ip){
        // 校验Ip
        if(!StringUtils.isEmpty(v4ip)){
            boolean flag = Ipv4Util.verifyIp(v4ip);
            if(flag){
                Map params = new HashMap();
                params.clear();
                params.put("v4ip", v4ip);
                params.put("terminalId", id);
                List<Terminal> terminals = this.terminalService.selectObjByMap(params);
                if(terminals.size() > 0){
                    return ResponseUtil.badArgument("Ip已存在");
                }return ResponseUtil.ok();
            }else{
                return ResponseUtil.badArgument("Ip格式错误");
            }
        }
        return ResponseUtil.badArgument("Ip为空");
    }

    @PostMapping("/save")
    public Object save(@RequestBody Terminal instance){
        // 验证名称是否唯一
        Map params = new HashMap();
        if(instance.getId() != null && !instance.getId().equals("")){
            Terminal terminal = this.terminalService.selectObjById(instance.getId());
            if(terminal == null){
                return ResponseUtil.badArgument("终端不存在");
            }
        }
        // 验证Ip唯一性
//        if(instance.getV4ip() == null || instance.getV4ip().equals("")){
//            return ResponseUtil.badArgument("请输入有效IP");
//        }
        if(instance.getV4ip() != null && !instance.getV4ip().equals("")){
            // 验证ip合法性
            boolean flag =  Ipv4Util.verifyIp(instance.getV4ip());
            if(!flag){
                return ResponseUtil.badArgument("ip不合法");
            }
            params.clear();
            params.put("v4ip", instance.getV4ip());
            params.put("terminalId", instance.getId());
            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
            if(terminals.size() > 0){
                return ResponseUtil.badArgument("IP重复");
            }
        }
//      if(Strings.isBlank(instance.getName())){
//            params.clear();
//            params.put("name", instance.getName());
//            params.put("terminalId", instance.getId());
//            List<Terminal> Terminals = this.terminalService.selectObjByMap(params);
//            if(Terminals.size() > 0){
//                return ResponseUtil.badArgumentRepeatedName();
//            }
//        }
        // 验证资产编号唯一性
        if(instance.getAsset_number() != null && !instance.getAsset_number().isEmpty()){
            params.clear();
            params.put("asset_number", instance.getAsset_number());
            params.put("terminalId", instance.getId());
            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
            if(terminals.size() > 0){
                Terminal terminal = terminals.get(0);
                return ResponseUtil.badArgument("资产编号与(" + terminal.getName() + ")设备重复");
            }
        }

//        if(instance.getClient_hostname() != null && !instance.getClient_hostname().isEmpty()){
//            params.clear();
//            params.put("client_hostname", instance.getName());
//            params.put("terminalId", instance.getId());
//            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
//            if(terminals.size() > 0){
//                Terminal terminal = terminals.get(0);
//                return ResponseUtil.badArgument("主机名与(" + terminal.getName() + ")设备重复");
//            }
//        }

        // 验证日期
        if(instance.getWarranty_time() != null && instance.getPurchase_time() != null){
            if(instance.getWarranty_time().before(instance.getPurchase_time())){
                return ResponseUtil.badArgument("过保时间必须大于采购时间");
            }
        }

        // 验证厂商
        if(instance.getVendorId() != null && !instance.getVendorId().equals("")){
            Vendor vendor = this.vendorService.selectObjById(instance.getVendorId());
            if(vendor == null){
                return ResponseUtil.badArgument("请输入正确品牌参数");
            }
        }

        // 验证项目
        if(instance.getProjectId() != null && !instance.getProjectId().equals("")){
            Project project = this.projectService.selectObjById(instance.getProjectId());
            if(project == null){
                return ResponseUtil.badArgument("请输入正确项目参数");
            }
        }

        // 设备类型
        if(instance.getDeviceTypeId() != null){
            DeviceType deviceType = this.deviceTypeService.selectObjById(instance.getDeviceTypeId());
            if(deviceType == null){
                return ResponseUtil.badArgument("请选择设备类型");
            }else{
                if(Strings.isBlank(instance.getName())){
                    instance.setName(deviceType.getName());
                }
            }
        }
        if(instance.getUuid() != null && !instance.getUuid().equals("")){
            instance.setFrom(3);
            instance.setInterfaceStatus(1);
        }
        if(instance.getInterfaceName() != null && !instance.getInterfaceName().equals("")){
            instance.setIndex(instance.getInterfaceName().replace("Port", ""));
        }
        boolean flag = this.terminalService.save(instance);
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @GetMapping("/count")
    public Object count(){
        Map terminal = this.terminalService.terminalCount();
        return ResponseUtil.ok(terminal);
    }

//
//    // 终端修改上联设备
//    @PutMapping("/update")
//    public Object update(@RequestParam String uuid, String id){
//        NetworkElement networkElement  = this.networkElementService.selectAccessoryByUuid(uuid);
//        if(networkElement != null){
//            Terminal terminal = this.terminalService.selectObjById(Long.parseLong(id));
//            if(terminal != null){
//                terminal.setDeviceName(networkElement.getDeviceName());
//                terminal.setInterfaceName(networkElement.getInterfaceName());
//                terminal.setUuid(networkElement.getUuid());
//                int i = this.terminalService.update(terminal);
//                if(i >= 1){
//                    return ResponseUtil.ok();
//                }
//                return ResponseUtil.error();
//            }
//        }
//        return ResponseUtil.badArgument();
//    }
//
//    @ApiOperation("批量修改终端为资产终端")
//    @PutMapping("/batch/update")
//    public Object editTerminal(@RequestParam Long[] ids){
//        if(ids.length > 0){
//            int i = this.terminalService.editTerminalType(ids);
//            if(i >= 1){
//                return ResponseUtil.ok();
//            }
//        }
//        return ResponseUtil.badArgument("请选择终端");
//    }
//
////    @DeleteMapping(value = {"/{id}","/{ids}"})
    @DeleteMapping
    public Object delete(Long id, String ids){
        if(Strings.isNotBlank(ids) && ids.split(",").length > 0){
            String[] idList = ids.split(",");
            for (String s : idList) {
                int i = this.terminalService.delete(Long.parseLong(s));
            }
            return ResponseUtil.ok();
        }else{
            Terminal terminal = this.terminalService.selectObjById(id);
            if(terminal != null){
                int i = this.terminalService.delete(id);
                if(i >= 1){
                    return ResponseUtil.ok();
                }else{
                    return ResponseUtil.error();
                }
            }
        }
        return ResponseUtil.badArgument();
    }

    @DeleteMapping("/empty")
    public Result empty(){
        boolean i = this.terminalService.deleteObjByType(0);
        if(i){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

//
////    @PutMapping
////    public Object update(@RequestBody Terminal instance){
////        if(instance.getDeviceTypeId() != null && !instance.getDeviceTypeId().equals("")){
////            DeviceType deviceType = this.deviceTypeService.selectObjById(instance.getDeviceTypeId());
////            if(deviceType == null){
////                return ResponseUtil.badArgument();
////            }
////        }
////        int i = this.terminalService.update(instance);
////        if(i >= 1){
////            return ResponseUtil.ok();
////        }else{
////            return ResponseUtil.error("保存失败");
////        }
////    }
//
//    @Value("${batchImportTerminalFileName}")
//    private String batchImportDeviceFileName;
//    @Value("${batchImportFilePath}")
//    private String batchImportFilePath;
//
//    @ApiOperation("下载模板")
//    @GetMapping("/downTemp")
//    public Object downTemplate(HttpServletResponse response) {
//        boolean flag = DownLoadFileUtil.downloadTemplate(this.batchImportFilePath, this.batchImportDeviceFileName, response);
//        if(flag){
//            return ResponseUtil.ok();
//        }else{
//            return ResponseUtil.error();
//        }
//    }


}
