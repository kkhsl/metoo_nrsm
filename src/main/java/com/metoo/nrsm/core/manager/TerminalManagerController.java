package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.core.manager.utils.MacUtils;
import com.metoo.nrsm.core.manager.utils.TerminalUtils;
import com.metoo.nrsm.core.mapper.TerminalMacIpv6Mapper;
import com.metoo.nrsm.core.mapper.UnitMapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.file.DownLoadFileUtil;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.poi.ExcelUtils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

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
    @Autowired
    private IUnitService unitService;
    @Autowired
    private UnitMapper unitMapper;
    @Autowired
    private TerminalUtils terminalUtils;

    @GetMapping("/vdt")
    public Result vdt(String ip) {
        if (StringUtils.isNotEmpty(ip)) {
            Map params = new HashMap();
            params.put("deviceIp", ip);
            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
            // 写入终端的设备名
            if (!terminals.isEmpty()) {
                for (Terminal terminal : terminals) {
                    params.clear();
                    params.put("deviceUuid", terminal.getDeviceUuid());
                    params.put("deviceTypeId", 34);
                    List<Terminal> vmHosts = this.terminalService.selectObjByMap(params);
                    if (!vmHosts.isEmpty()) {
                        DeviceType deviceType = this.deviceTypeService.selectObjById(34L);
                        if (deviceType != null) {
                            terminal.setDeviceName(deviceType.getName());
                        }
                    }
                }
                return ResponseUtil.ok(terminals);
            }
        }
        return ResponseUtil.ok();
    }

    @PostMapping("/list")
    public Object list(@RequestBody TerminalDTO dto) {
        User user = ShiroUserHolder.currentUser();
        Unit loginUnit = unitMapper.selectObjById(user.getUnitId());  //登录的单位
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
        Page<Terminal> page=null;

        if (loginUnit.getUnitLevel()!=null){
            if (loginUnit.getUnitLevel()==0){
                page = this.terminalService.selectObjByConditionQuery(dto);
            }else {
                dto.setUnitId(user.getUnitId());
                page = this.terminalService.selectObjByConditionQuery(dto);
            }
        }else {
            dto.setUnitId(user.getUnitId());
            page = this.terminalService.selectObjByConditionQuery(dto);
        }


        if (page.size() > 0) {
            page.getResult().stream().forEach(terminal -> {
                if (terminal.getV6ip() != null && terminal.getV6ip().toLowerCase().startsWith("fe80")) {
                    terminal.setV6ip(null);
                }
                if (terminal.getV6ip1() != null && terminal.getV6ip1().toLowerCase().startsWith("fe80")) {
                    terminal.setV6ip1(null);
                }
                if (terminal.getV6ip2() != null && terminal.getV6ip2().toLowerCase().startsWith("fe80")) {
                    terminal.setV6ip2(null);
                }
                if (terminal.getV6ip3() != null && terminal.getV6ip3().toLowerCase().startsWith("fe80")) {
                    terminal.setV6ip3(null);
                }
                if (terminal.getDeviceTypeId() != null && !terminal.getDeviceTypeId().equals("")) {
                    DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                    if (deviceType != null) {
                        terminal.setDeviceTypeName(deviceType.getName());
                    }
                }
                if (terminal.getVendorId() != null && !terminal.getVendorId().equals("")) {
                    Vendor vendor = this.vendorService.selectObjById(terminal.getVendorId());
                    if (vendor != null) {
                        terminal.setVendorName(vendor.getName());
                    }
                }

                if (terminal.getProjectId() != null && !terminal.getProjectId().equals("")) {
                    Project project = this.projectService.selectObjById(terminal.getProjectId());
                    if (project != null) {
                        terminal.setProjectName(project.getName());
                    }
                }
                macUtils.terminalSetMacVendor(terminal);
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

        List<Unit> unitList = unitMapper.selectAllQuery();
        map.put("unitList", unitList);
        // 项目
        params.clear();
        List<Project> projectList = this.projectService.selectObjByMap(params);
        map.put("project", projectList);

        return ResponseUtil.ok(new PageInfo<Terminal>(page, map));
    }

//    @GetMapping("/unit")
//    public Result unitTerminal(){
//        List<TerminalUnit> terminalUnitList = terminalUnitService.selectObjAndTerminalByMap(null);
////        for (TerminalUnit terminalUnit : terminalUnitList) {
////            macUtils.terminalJoint(terminalUnit.getTerminalList());
////        }
//        return ResponseUtil.ok(terminalUnitList);
////        Result result = this.unitService.selectAllQuery();
////        return result;
//    }

    @GetMapping("/unit")
    public Result unitTerminal() {
        User user = ShiroUserHolder.currentUser();
        if(user.getUnitId() == null || "".equals(user.getUnitId())){
            return ResponseUtil.badArgument("用户信息错误，未分配单位");
        }
        List<TerminalUnit> terminalUnitList = null;
        Unit unit = unitService.selectObjById(user.getUnitId());
        if (unit.getUnitLevel() != null && unit.getUnitLevel() == 0) {
            terminalUnitList = terminalUnitService.selectObjAndTerminalByMap(Collections.emptyMap());
        } else {
            Map params = new HashMap();
            params.put("unitId", user.getUnitId());
            terminalUnitList = terminalUnitService.selectObjAndTerminalByMap(params);
        }
        if(terminalUnitList.size() > 0){
            for (TerminalUnit terminalUnit : terminalUnitList) {
                if(terminalUnit.getTerminalList().size() > 0){
                    for (Terminal terminal : terminalUnit.getTerminalList()) {
                        terminalUtils.completeTerminal(terminal);
                    }
                }
            }
        }
        return ResponseUtil.ok(terminalUnitList);
    }


    @GetMapping("/ipv6")
    public void ipv6() {
        List<Terminal> terminalList = this.terminalService.selectObjByMap(null);
        if (terminalList.size() > 0) {
            for (Terminal terminal : terminalList) {
                if (StringUtil.isNotEmpty(terminal.getV6ip())) {
                    TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Mapper.getMacByMacAddress(terminal.getMac());
                    if (terminalMacIpv6 == null) {
                        this.terminalMacIpv6Mapper.insertMac(terminal.getMac(), 1);
                    }
                }
            }
        }

    }

    @GetMapping("/unit/history")
    public Result unitTerminalHistory() {

        User user = ShiroUserHolder.currentUser();

        Unit unit = unitService.selectObjById(user.getUnitId());

        Map params = new HashMap();
        if(unit.getUnitLevel() == null){
            params.put("unitId", unit.getId());
        }
        params.put("time", "2024-09-16 11:31:00");

        List<TerminalUnit> terminalUnitList = terminalUnitService.selectObjAndTerminalHistoryByMap(params);
        for (TerminalUnit terminalUnit : terminalUnitList) {
            if (terminalUnit.getTerminalList().size() > 0) {
                for (Terminal terminal : terminalUnit.getTerminalList()) {
                    if (StringUtil.isNotEmpty(terminal.getMac())) {
                        TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Service.getMacByMacAddress(terminal.getMac());
                        if (terminalMacIpv6 != null) {
                            terminal.setIsIpv6(1);
                        } else {
                            terminal.setIsIpv6(0);
                        }
                    }
                }
            }
        }
        return ResponseUtil.ok(terminalUnitList);
    }

    @GetMapping("/add")
    public Object add() {
        // 设备类型
        Map data = new HashMap();
        Map parmas = new HashMap();
        parmas.put("diff", 1);
        parmas.put("orderBy", "sequence");
        parmas.put("orderType", "DESC");
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
        data.put("deviceTypeList", deviceTypeList);
        // 厂商
        List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
        data.put("vendor", vendors);
        // 项目
        Map params = new HashMap();
        List<Project> projectList = this.projectService.selectObjByMap(params);
        data.put("project", projectList);

        List<Unit> unit2s = this.unitService.selectUnitAll();
        data.put("unitList", unit2s);

        return ResponseUtil.ok(data);
    }

    @GetMapping("/update/{id}")
    public Object update(@PathVariable Long id) {
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        Terminal terminal = this.terminalService.selectObjById(id);
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
        Map data = new HashMap();
        data.put("terminal", terminal);

        User user = ShiroUserHolder.currentUser();
        // 设备类型
        Map parmas = new HashMap();
        parmas.put("diff", 1);
        parmas.put("orderBy", "sequence");
        parmas.put("orderType", "DESC");
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
        data.put("deviceTypeList", deviceTypeList);

        // 厂商
        List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
        data.put("vendor", vendors);
        // 项目
        Map params = new HashMap();
        List<Project> projectList = this.projectService.selectObjByMap(params);
        data.put("project", projectList);

        List<Unit> unit2s = this.unitService.selectUnitAll();
        data.put("unitList", unit2s);


        return ResponseUtil.ok(data);
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
                List<Terminal> terminals = this.terminalService.selectObjByMap(params);
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
    public Object save(@RequestBody Terminal instance) {
        // 验证名称是否唯一
        Map params = new HashMap();
        Unit unit = null;
        if (instance.getId() != null && !instance.getId().equals("")) {
            Terminal terminal = this.terminalService.selectObjById(instance.getId());
            if (terminal == null) {
                return ResponseUtil.badArgument("终端不存在");
            }
        }
        // 验证Ip唯一性
//        if(instance.getV4ip() == null || instance.getV4ip().equals("")){
//            return ResponseUtil.badArgument("请输入有效IP");
//        }
        if (instance.getV4ip() != null && !instance.getV4ip().equals("")) {
            // 验证ip合法性
            boolean flag = Ipv4Util.verifyIp(instance.getV4ip());
            if (!flag) {
                return ResponseUtil.badArgument("ip不合法");
            }
            params.clear();
            params.put("v4ip", instance.getV4ip());
            params.put("terminalId", instance.getId());
            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
            if (terminals.size() > 0) {
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
        if (instance.getAsset_number() != null && !instance.getAsset_number().isEmpty()) {
            params.clear();
            params.put("asset_number", instance.getAsset_number());
            params.put("terminalId", instance.getId());
            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
            if (terminals.size() > 0) {
                Terminal terminal = terminals.get(0);
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

        // 验证部门
        if (instance.getUnitId() != null && !instance.getUnitId().equals("")) {
            unit = unitMapper.selectObjById(instance.getUnitId());
            if (unit == null) {
                return ResponseUtil.badArgument("请输入正确单位/部门");
            } else {
                instance.setUnitName(unit.getUnitName());
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
        } else {
            return ResponseUtil.error();
        }
    }

    @GetMapping("/count")
    public Object count() {
        Map terminal = this.terminalService.terminalCount();
        return ResponseUtil.ok(terminal);
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
            Terminal terminal = this.terminalService.selectObjById(id);
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

    @DeleteMapping("/empty")
    public Result empty() {
        boolean i = this.terminalService.deleteObjByType(0);
        if (i) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @Value("${batchImportTerminalFileName}")
    private String batchImportDeviceFileName;
    @Value("${batchImportFilePath}")
    private String batchImportFilePath;

    @ApiOperation("下载终端批量上传模板")
    @GetMapping("/downTemp")
    public Object downTemplate(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        boolean flag = DownLoadFileUtil.downloadTemplate(this.batchImportFilePath, this.batchImportDeviceFileName, response);
        if (flag) {
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.error();
        }
    }


    @ApiOperation("设备批量导入")
    @PostMapping("/import")
    public Object importExcel(@RequestPart("file") MultipartFile file) throws Exception {
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename().toLowerCase();
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            if (suffix.equals("xlsx") || suffix.equals("xls")) {
                List<Terminal> terminals = ExcelUtils.readMultipartFile(file, Terminal.class);
                // 校验表格数据是否符号要求
                String tips = "";
                for (Terminal terminal : terminals) {
                    if (!terminal.getRowTips().isEmpty()) {
                        tips = terminal.getRowTips();
                        break;
                    }
                }
                if (!tips.isEmpty()) {
                    return ResponseUtil.badArgument(tips);
                }
                if (terminals.size() > 0) {
                    String msg = "";
                    Map params = new HashMap();
                    List<Terminal> terminalList = new ArrayList<>();
                    for (int i = 0; i < terminals.size(); i++) {
                        Terminal terminal = terminals.get(i);
                        if (terminal.getName() == null || terminal.getName().equals("")) {
                            msg = "第" + (i + 2) + "行,终端名称不能为空";
                            break;
                        }
                        /*else {
                            params.clear();
                            params.put("name", terminal.getName());
                            List<Terminal> terminals1 = this.terminalService.selectObjByMap(params);
                            if (terminals1.size() > 0) {
                                msg = "第" + (i + 2) + "行, 设备已存在";
                                break;
                            }
                        }*/
                        User user = ShiroUserHolder.currentUser();
                        // 验证资产编号唯一性
                        if (terminal.getAsset_number() != null && !terminal.getAsset_number().isEmpty()) {
                            params.clear();
                            params.put("asset_number", terminal.getAsset_number());
                            params.put("deviceId", terminal.getId());
                            List<Terminal> terminalList1 = this.terminalService.selectObjByMap(params);
                            if (terminalList1.size() > 0) {
                                Terminal terminal1 = terminalList1.get(0);
                                return ResponseUtil.badArgument("资产编号与(" + terminal1.getName() + ")设备重复");
                            }
                        }
                        // 验证主机名是否重复
                        /*if (terminal.getClient_hostname() != null && !terminal.getClient_hostname().isEmpty()) {
                            params.clear();
                            params.put("host_name", terminal.getClient_hostname());
                            params.put("deviceId", terminal.getId());
                            List<Terminal> terminals1 = this.terminalService.selectObjByMap(params);
                            if (terminals1.size() > 0) {
                                Terminal rsmsDevice = terminals1.get(0);
                                return ResponseUtil.badArgument("主机名与(" + rsmsDevice.getName() + ")设备重复");
                            }
                        }*/
                        if (terminal.getV4ip() == null || terminal.getV4ip().equals("")) {
                            msg = "第" + (i + 2) + "行,v4IP地址不能为空";
                            break;
                        }

                        if (terminal.getV4ip() != null && !terminal.getV4ip().equals("")) {
                            boolean flag = Ipv4Util.verifyIp(terminal.getV4ip());
                            if (flag) {
                                params.clear();
                                params.put("ip", terminal.getV4ip());
                                List<Terminal> terminals1 = this.terminalService.selectObjByMap(params);
                                if (terminals1.size() > 0) {
                                    msg = "第" + (i + 2) + "行, IP已存在";
                                    break;
                                }
                            } else {
                                msg = "第" + (i + 2) + "行, IP格式错误";
                                break;
                            }
                        }

                        //状态
                        if (terminal.getOnline() == null || terminal.getOnline().equals("")) {
                            msg = "第" + (i + 2) + "行, 状态不能为空";
                            break;
                        }

                        //资产
                        if (terminal.getType() == null || terminal.getType().equals("")) {
                            terminal.setType(0);
                        }

                        if (terminal.getType() != null || !terminal.getType().equals("")) {
                            if (terminal.getType().equals("1")){
                                terminal.setType(1);
                            }else {
                                terminal.setType(0);
                            }
                        }

                        //单位
                        if (terminal.getUnitName() == null || terminal.getUnitName().equals("")) {
                            msg = "第" + (i + 2) + "行,所属部门/单位不能为空";
                            break;
                        }

                        if (terminal.getUnitName() != null && !terminal.getUnitName().equals("")) {
                            List<Unit> units = unitMapper.selectByUnitName(terminal.getUnitName());
                            if (units.size() == 0) {
                                msg = "第" + (i + 2) + "行,所属部门/单位不存在";
                                break;
                            } else {
                                terminal.setUnitId(units.get(0).getId());
                            }
                        }

                        // 设备类型
                        if (terminal.getDeviceTypeName() != null && !terminal.getDeviceTypeName().equals("")) {
                            params.clear();
                            params.put("name", terminal.getDeviceTypeName());
                            List<DeviceType> deviceType = this.deviceTypeService.selectObjByMap(params);
                            if (deviceType == null) {
                                msg = "第" + (i + 2) + "行,设备类型不存在";
                                break;
                            } else {
                                terminal.setDeviceTypeId(deviceType.get(0).getId());
                            }
                        }

                        // 品牌
                        if (terminal.getVendorName() != null && terminal.getVendorName().equals("")) {
                            Vendor vendor = this.vendorService.selectObjByName(terminal.getVendorName());
                            if (vendor == null) {
                                msg = "第" + (i + 2) + "行,品牌不存在";
                                break;
                            } else {
                                terminal.setVendorId(vendor.getId());
                            }
                        }
                        // 项目
                        if (terminal.getProjectName() != null && !terminal.getProjectName().equals("")) {
                            params.clear();
                            params.put("name", terminal.getProjectName());
                            List<Project> projects = this.projectService.selectObjByMap(params);
                            if (projects.size() <= 0) {
                                msg = "第" + (i + 2) + "行,项目不存在";
                                break;
                            } else {
                                Project project = projects.get(0);
                                terminal.setProjectId(project.getId());
                            }
                        }
                        // 验证日期
                        if (terminal.getWarranty_time() != null && terminal.getPurchase_time() != null) {
                            if (terminal.getWarranty_time().before(terminal.getPurchase_time())) {
                                return ResponseUtil.badArgument("过保时间必须大于采购时间");
                            }
                        }
                        terminalList.add(terminal);
                    }
                    if (msg.isEmpty()) {
                        int i = this.terminalService.batchInsert(terminalList);
                        if (i > 0) {
                            return ResponseUtil.ok();
                        } else {
                            return ResponseUtil.error();
                        }
                    } else {
                        return ResponseUtil.badArgument(msg);
                    }
                } else {
                    return ResponseUtil.badArgument("文件无数据");
                }
            } else {
                return ResponseUtil.badArgument("文件格式错误，请使用标准模板上传");
            }
        }
        return ResponseUtil.badArgument("文件不存在");
    }

    @ApiOperation("设备导出")
    @GetMapping(value = "/export")
    public Object export(HttpServletResponse response, Terminal terminal) {

        if (StringUtils.isBlank(terminal.getExcelName())) {
            terminal.setExcelName("终端设备台账" + DateTools.getCurrentDate(new Date()));//  +".xls"
        }
        Map params = new HashMap();
        params.put("ids", terminal.getIds());
        List<Terminal> devices = this.terminalService.selectObjByMap(params);
        if (devices.size() > 0) {
            for (Terminal device : devices) {
                if (device.getDeviceTypeId() != null && !device.getDeviceTypeId().equals("")) {
                    DeviceType instance = this.deviceTypeService.selectObjById(device.getDeviceTypeId());
                    device.setDeviceTypeName(instance.getName());
                }
                if (device.getVendorId() != null && !device.getVendorId().equals("")) {
                    Vendor instance = this.vendorService.selectObjById(device.getVendorId());
                    device.setVendorName(instance.getName());
                }

                if (device.getProjectId() != null && !device.getProjectId().equals("")) {
                    Project instance = this.projectService.selectObjById(device.getProjectId());
                    device.setProjectName(instance.getName());
                }

            }
            List<List<Object>> sheetDataList = ExcelUtils.getSheetData(devices);
            ExcelUtils.export(response, terminal.getExcelName(), sheetDataList);
        }
        return ResponseUtil.ok();
    }




}
