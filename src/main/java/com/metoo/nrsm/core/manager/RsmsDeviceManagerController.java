package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.annotation.OperationLogAnno;
import com.metoo.nrsm.core.config.annotation.OperationType;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.PlantRoomDTO;
import com.metoo.nrsm.core.dto.RsmsDeviceDTO;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.file.DownLoadFileUtil;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.poi.ExcelUtils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Api("设备")
@RequestMapping("/admin/rsms/device")
@RestController
public class RsmsDeviceManagerController {

    private static final Logger LOG = LogManager.getLogger(RsmsDeviceManagerController.class);

    @Autowired
    private IRsmsDeviceService rsmsDeviceService;
    @Autowired
    private IPlantRoomService plantRoomService;
    @Autowired
    private IRackService rackService;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private IVendorService vendorService;
    @Autowired
    private IProjectService projectService;
    @Autowired
    private INetworkElementService networkElementService;

    @GetMapping("/get")
    public Object get(String id) {
        RsmsDevice rsmsDevice = this.rsmsDeviceService.getObjById(Long.parseLong(id));
        return ResponseUtil.ok(rsmsDevice);
    }

    @RequestMapping("/list")
    public Object list(@RequestBody RsmsDeviceDTO dto) {
//        User user = ShiroUserHolder.currentUser();
//        dto.setUserId(user.getId());
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
        Page<RsmsDevice> page = this.rsmsDeviceService.selectConditionQuery(dto);
        Map map = new HashMap();
        // 设备类型term
        Map parmas = new HashMap();
        parmas.put("diff", 0);
        parmas.put("orderBy", "sequence");
        parmas.put("orderType", "DESC");
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
        map.put("deviceTypeList", deviceTypeList);
        // 厂商
        List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
        map.put("vendor", vendors);
        // 项目
        Map params = new HashMap();
//        params.put("userId", user.getId());
        List<Project> projectList = this.projectService.selectObjByMap(params);
        map.put("project", projectList);
        return ResponseUtil.ok(new PageInfo<RsmsDevice>(page, map));
    }

    @RequestMapping("/type/list")
    public Object type() {
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectCountByJoin();
        return ResponseUtil.ok(deviceTypeList);
    }

    @GetMapping("/add")
    public Object add() {
        Map map = new HashMap();
        // 设备类型
        Map parmas = new HashMap();
        parmas.put("diff", 0);
        parmas.put("orderBy", "sequence");
        parmas.put("orderType", "DESC");
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
        map.put("deviceTypeList", deviceTypeList);

        PlantRoomDTO dto = new PlantRoomDTO();
        dto.setCurrentPage(1);
        dto.setPageSize(100000);
        Page<PlantRoom> page = this.plantRoomService.findBySelectAndRack(dto);
        map.put("plantRoomList", page.getResult());

//        User user = ShiroUserHolder.currentUser();
        // 厂商
        List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
        map.put("vendor", vendors);
        // 项目
        Map params = new HashMap();
//        params.put("userId", user.getId());
        List<Project> projectList = this.projectService.selectObjByMap(params);
        map.put("project", projectList);
//        params.clear();
//        params.put("orderBy", "sequence");
//        params.put("orderType", "desc");
//        List<Department> departments= this.departmentService.selectObjByMap(params);
//        map.put("department", departments);
        return ResponseUtil.ok(map);
    }

    @GetMapping("/update")
    public Object update(String id) {
        RsmsDevice rsmsDevice = this.rsmsDeviceService.getObjById(Long.parseLong(id));
        if (rsmsDevice != null) {
            Map map = new HashMap();
            // 机房
            PlantRoom plantRoom = this.plantRoomService.getObjById(rsmsDevice.getPlantRoomId());
            // 机柜
            Rack rack = this.rackService.getObjById(rsmsDevice.getRackId());
//            List<Rack> rackList = this.rackService.query(null);

            PlantRoomDTO dto = new PlantRoomDTO();
            dto.setCurrentPage(1);
            dto.setPageSize(100000);
            Page<PlantRoom> page = this.plantRoomService.findBySelectAndRack(dto);
            map.put("plantRoom", plantRoom);
            map.put("plantRoomList", page.getResult());
            map.put("rack", rack);
//            map.put("rackList", rackList);
            // 设备类型
            Map parmas = new HashMap();
            parmas.put("diff", 0);
            parmas.put("orderBy", "sequence");
            parmas.put("orderType", "DESC");
            List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
            map.put("deviceTypeList", deviceTypeList);
            map.put("device", rsmsDevice);

//            User user = ShiroUserHolder.currentUser();
            // 厂商
            List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
            map.put("vendor", vendors);
            // 项目
            Map params = new HashMap();
//            params.put("userId", user.getId());
            List<Project> projectList = this.projectService.selectObjByMap(params);
            map.put("project", projectList);

            return ResponseUtil.ok(map);
        }
        return ResponseUtil.badArgument();
    }


    @GetMapping("/detail")
    public Object detail(@RequestParam(value = "uuid") String uuid) {
        if (!StringUtils.isEmpty(uuid)) {
            Map params = new HashMap();
            params.put("uuid", uuid);
            List<RsmsDevice> rsmsDevices = this.rsmsDeviceService.selectObjByMap(params);
            if (rsmsDevices.size() > 0) {
                RsmsDevice obj = rsmsDevices.get(0);
                // 机房
                PlantRoom plantRoom = this.plantRoomService.getObjById(obj.getPlantRoomId());
                // 机柜
                Rack rack = this.rackService.getObjById(obj.getRackId());
                obj.setPlantRoom(plantRoom);
                obj.setRack(rack);
                return ResponseUtil.ok(obj);
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument("Ip为空");
    }


    @GetMapping("/verify")
    public Object verifyIp(@RequestParam(value = "id", required = false) Long id,
                           @RequestParam(value = "ip", required = true) String ip) {
        // 校验Ip
        if (!StringUtils.isEmpty(ip)) {
            boolean flag = Ipv4Util.verifyIp(ip);
            if (flag) {
                Map params = new HashMap();
                params.clear();
                params.put("ip", ip);
                params.put("deviceId", id);
                List<RsmsDevice> deviceListIp = this.rsmsDeviceService.selectObjByMap(params);
                if (deviceListIp.size() > 0) {
                    return ResponseUtil.badArgument("Ip已存在");
                }
                return ResponseUtil.ok();
            } else {
                return ResponseUtil.badArgument("Ip格式错误");
            }
        }
        return ResponseUtil.badArgument("Ip为空");
    }

    @OperationLogAnno(operationType = OperationType.CREATE, name = "device")
    @RequestMapping("/save")
    public Object save(@RequestBody RsmsDevice instance) {
        if (instance == null) {
            return ResponseUtil.badArgument();
        }
        Map params = new HashMap();
        // 验证名称是否唯一
        if (instance.getName() != null && !instance.getName().isEmpty()) {
            params.clear();
            params.put("name", instance.getName());
            params.put("deviceId", instance.getId());
            List<RsmsDevice> rsmsDeviceList = this.rsmsDeviceService.selectObjByMap(params);
            if (rsmsDeviceList.size() > 0) {
                return ResponseUtil.badArgumentRepeatedName();
            }
        }
        // 验证资产编号唯一性
        if (instance.getAsset_number() != null && !instance.getAsset_number().isEmpty()) {
            params.clear();
            params.put("asset_number", instance.getAsset_number());
            params.put("deviceId", instance.getId());
            List<RsmsDevice> rsmsDeviceList = this.rsmsDeviceService.selectObjByMap(params);
            if (rsmsDeviceList.size() > 0) {
                RsmsDevice rsmsDevice = rsmsDeviceList.get(0);
                return ResponseUtil.badArgument("资产编号与(" + rsmsDevice.getName() + ")设备重复");
            }
        }
        // 验证主机名是否重复
        if (instance.getHost_name() != null && !instance.getHost_name().isEmpty()) {
            params.clear();
            params.put("host_name", instance.getHost_name());
            params.put("deviceId", instance.getId());
            List<RsmsDevice> rsmsDeviceList = this.rsmsDeviceService.selectObjByMap(params);
            if (rsmsDeviceList.size() > 0) {
                RsmsDevice rsmsDevice = rsmsDeviceList.get(0);
                return ResponseUtil.badArgument("主机名与(" + rsmsDevice.getName() + ")设备重复");
            }
        }

        if (instance.getPlantRoomId() != null && !instance.getPlantRoomId().equals("")) {
            PlantRoom plantRoom = this.plantRoomService.getObjById(instance.getPlantRoomId());
            if (plantRoom != null) {
                instance.setPlantRoomId(plantRoom.getId());
                instance.setPlantRoomName(plantRoom.getName());
            } else {
                return ResponseUtil.badArgument("机房参数错误");
            }
        }
        if (instance.getRackId() != null && !instance.getRackId().equals("")) {
            Rack rack = this.rackService.getObjById(instance.getRackId());
            if (rack != null) {
                int start = 0;
                int size = 0;
                if (instance.getStart() != null) {
                    start = instance.getStart();
                }
                if (instance.getStart() != null) {
                    size = instance.getSize();
                }
                int length = (start - 1) + size;
                if (length > rack.getSize()) {
                    return ResponseUtil.badArgument("超出可使用机柜长度");
                }
                // 判断是否修改位置
                boolean isVerify = true;
                if (instance.getId() != null) {
                    RsmsDevice rsmsDevice = this.rsmsDeviceService.getObjById(instance.getId());
                    if (rsmsDevice != null) {
                        if (rsmsDevice.getSize() == instance.getSize() && rsmsDevice.getStart() == instance.getStart()) {
                            isVerify = false;
                        }
                    }
                }
                if (instance.getStart() == null || instance.getStart() <= 0 || instance.getSize() == null || instance.getSize() <= 0) {
                    isVerify = false;
                }
                // 判断改长度内是否存在设备
                if (isVerify) {
                    boolean verify = this.rackService.verifyRack(rack, instance.getStart(), instance.getSize(), instance.isRear(), instance.getId());
                    if (!verify) {
                        return ResponseUtil.badArgument("当前位置已有设备");
                    }
                }
                instance.setRackId(rack.getId());
                instance.setRackName(rack.getName());
            } else {
                return ResponseUtil.badArgument("机柜参数错误");
            }
        }

        // 验证日期
        if (instance.getWarranty_time() != null && instance.getPurchase_time() != null) {
            if (instance.getWarranty_time().before(instance.getPurchase_time())) {
                return ResponseUtil.badArgument("过保时间必须大于采购时间");
            }
        }
        // 验证厂商
        Vendor vendor = this.vendorService.selectObjById(instance.getVendorId());
        if (vendor != null) {
            instance.setVendorName(vendor.getName());
        }

        // 验证项目
        if (instance.getProjectId() != null) {
            Project project = this.projectService.selectObjById(instance.getProjectId());
            if (project == null) {
                return ResponseUtil.badArgument("请输入正确项目参数");
            }
        }

        int flag = this.rsmsDeviceService.save(instance);
        if (flag != 0) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("设备保存失败");
    }

    @ApiOperation("删除设备")
    @DeleteMapping("/del")
    public Object del(@RequestParam(value = "id") String id) {
        RsmsDevice instance = this.rsmsDeviceService.getObjById(Long.parseLong(id));
        NetworkElement networkElement = null;
        if (instance == null) {
            return ResponseUtil.badArgument("资源不存在");
        } else {
            networkElement = this.networkElementService.selectObjByUuid(instance.getUuid());
        }
        int flag = this.rsmsDeviceService.delete(Long.parseLong(id));
        if (flag != 0) {
            // 批量更新网元，取消同步至资产管理
            try {
                if (networkElement != null) {
                    networkElement.setSync_device(false);
                    try {
                        this.networkElementService.update(networkElement);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("设备删除失败");
    }

    @ApiOperation("批量设备")
    @DeleteMapping("/batch/del")
    public Object batchDel(@RequestParam(value = "ids") String ids) {
        String[] l = ids.split(",");
        List<String> list = Arrays.asList(l);
        List<String> uuids = new ArrayList<>();
        for (String id : list) {
            RsmsDevice instance = this.rsmsDeviceService.getObjById(Long.parseLong(id));
            if (instance == null) {
                return ResponseUtil.badArgument("设备不存在");
            } else {
                uuids.add(instance.getUuid());
            }
        }
        int flag = this.rsmsDeviceService.batchDel(ids);
        if (flag != 0) {
            // 批量更新网元，取消同步至资产管理
            try {
                for (String uuid : uuids) {
                    NetworkElement networkElement = this.networkElementService.selectObjByUuid(uuid);
                    if (networkElement != null) {
                        networkElement.setSync_device(false);
                        try {
                            this.networkElementService.update(networkElement);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("设备删除失败");
    }

    @ApiOperation("设备批量导入")
    @PostMapping("/import")
    public Object importExcel(@RequestPart("file") MultipartFile file) throws Exception {
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename().toLowerCase();
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            if (suffix.equals("xlsx") || suffix.equals("xls")) {
                List<RsmsDevice> devices = ExcelUtils.readMultipartFile(file, RsmsDevice.class);
                // 校验表格数据是否符号要求
                String tips = "";
                for (RsmsDevice device : devices) {
                    if (!device.getRowTips().isEmpty()) {
                        tips = device.getRowTips();
                        break;
                    }
                }
                if (!tips.isEmpty()) {
                    return ResponseUtil.badArgument(tips);
                }
                if (devices.size() > 0) {
                    String msg = "";
                    Map params = new HashMap();
                    List<RsmsDevice> rsmsDevices = new ArrayList<>();
                    for (int i = 0; i < devices.size(); i++) {
                        RsmsDevice device = devices.get(i);
                        if (device.getName() == null || device.getName().equals("")) {
                            msg = "第" + (i + 2) + "行,设备名不能为空";
                            break;
                        } else {
                            params.clear();
                            params.put("name", device.getName());
                            List<RsmsDevice> deviceList = this.rsmsDeviceService.selectObjByMap(params);
                            if (deviceList.size() > 0) {
                                msg = "第" + (i + 2) + "行, 设备已存在";
                                break;
                            }
                        }
                        User user = ShiroUserHolder.currentUser();
                        // 验证资产编号唯一性
                        if (device.getAsset_number() != null && !device.getAsset_number().isEmpty()) {
                            params.clear();
                            params.put("asset_number", device.getAsset_number());
                            params.put("deviceId", device.getId());
                            List<RsmsDevice> rsmsDeviceList = this.rsmsDeviceService.selectObjByMap(params);
                            if (rsmsDeviceList.size() > 0) {
                                RsmsDevice rsmsDevice = rsmsDeviceList.get(0);
                                return ResponseUtil.badArgument("资产编号与(" + rsmsDevice.getName() + ")设备重复");
                            }
                        }
                        // 验证主机名是否重复
                        if (device.getAsset_number() != null && !device.getAsset_number().isEmpty()) {
                            params.clear();
                            params.put("host_name", device.getHost_name());
                            params.put("deviceId", device.getId());
                            List<RsmsDevice> rsmsDeviceList = this.rsmsDeviceService.selectObjByMap(params);
                            if (rsmsDeviceList.size() > 0) {
                                RsmsDevice rsmsDevice = rsmsDeviceList.get(0);
                                return ResponseUtil.badArgument("主机名与(" + rsmsDevice.getName() + ")设备重复");
                            }
                        }
                        if (device.getIp() != null && !device.getIp().equals("")) {
                            boolean flag = Ipv4Util.verifyIp(device.getIp());
                            if (flag) {
                                params.clear();
                                params.put("ip", device.getIp());
                                List<RsmsDevice> deviceListIp = this.rsmsDeviceService.selectObjByMap(params);
                                if (deviceListIp.size() > 0) {
                                    msg = "第" + (i + 2) + "行, IP已存在";
                                    break;
                                }
                            } else {
                                msg = "第" + (i + 2) + "行, IP格式错误";
                                break;
                            }
                        }
                        // 设备类型
                        if (device.getDeviceTypeName() != null && !device.getDeviceTypeName().equals("")) {
                            params.clear();
                            params.put("name", device.getDeviceTypeName());
                            DeviceType deviceType = this.deviceTypeService.selectObjByName(device.getDeviceTypeName());
                            if (deviceType == null) {
                                msg = "第" + (i + 2) + "行,设备类型不存在";
                                break;
                            } else {
                                device.setDeviceTypeId(deviceType.getId());
                            }
                        }

                        // 品牌
                        if (device.getVendorName() != null && device.getVendorName().equals("")) {
                            Vendor vendor = this.vendorService.selectObjByName(device.getVendorName());
                            if (vendor == null) {
                                msg = "第" + (i + 2) + "行,品牌不存在";
                                break;
                            } else {
                                device.setVendorId(vendor.getId());
                            }
                        }
                        // 项目
                        if (device.getProjectName() != null && !device.getProjectName().equals("")) {
                            params.clear();
                            params.put("name", device.getProjectName());
                            List<Project> projects = this.projectService.selectObjByMap(params);
                            if (projects.size() <= 0) {
                                msg = "第" + (i + 2) + "行,项目不存在";
                                break;
                            } else {
                                Project project = projects.get(0);
                                device.setProjectId(project.getId());
                            }
                        }
                        if (device.getPlantRoomName() != null && !device.getPlantRoomName().equals("")) {
                            params.clear();
                            params.put("name", device.getPlantRoomName());
                            List<PlantRoom> plantRooms = this.plantRoomService.selectObjByMap(params);
                            if (plantRooms.size() >= 1) {
                                PlantRoom plantRoom = plantRooms.get(0);
                                device.setPlantRoomId(plantRoom.getId());
                                device.setPlantRoomName(plantRoom.getName());
                            } else {
                                msg = "第" + (i + 2) + "行,机房不存在";
                                break;
                            }
                            if (device.getRackName() != null && !device.getRackName().equals("")) {
                                params.clear();
                                params.put("name", device.getRackName());
                                params.put("plantRoomId", device.getPlantRoomId());
                                List<Rack> racks = this.rackService.selectObjByMap(params);
                                if (racks.size() >= 1) {
                                    Rack rack = racks.get(0);
                                    int start = 0;
                                    int size = 0;
                                    if (device.getStart() != null) {
                                        start = device.getStart();
                                    }
                                    if (device.getStart() != null) {
                                        size = device.getSize();

                                    }

                                    int length = (start - 1) + size;
                                    if (length > rack.getSize()) {
                                        return ResponseUtil.badArgument("超出可使用机柜长度");
                                    }
                                    // 判断是否修改位置
                                    boolean isVerify = true;
                                    if (device.getId() != null) {
                                        RsmsDevice rsmsDevice = this.rsmsDeviceService.getObjById(device.getId());
                                        if (rsmsDevice != null) {
                                            if (rsmsDevice.getSize() == device.getSize()
                                                    && rsmsDevice.getStart() == device.getStart()) {
                                                isVerify = false;
                                            }
                                        }
                                    }
                                    if (device.getStart() == null || device.getStart() <= 0
                                            || device.getSize() == null || device.getSize() <= 0) {
                                        isVerify = false;
                                    }
                                    // 判断改长度内是否存在设备
                                    if (isVerify) {
                                        boolean verify = this.rackService.verifyRack(rack, device.getStart(), device.getSize(),
                                                device.isRear(), device.getId());
                                        if (!verify) {
                                            return ResponseUtil.badArgument("当前位置已有设备");
                                        }
                                    }
                                    device.setRackId(rack.getId());
                                    device.setRackName(rack.getName());
                                } else {
                                    return ResponseUtil.badArgument("机柜不存在");
                                }
                            }
                        }
                        // 验证日期
                        if (device.getWarranty_time() != null && device.getPurchase_time() != null) {
                            if (device.getWarranty_time().before(device.getPurchase_time())) {
                                return ResponseUtil.badArgument("过保时间必须大于采购时间");
                            }
                        }
                        rsmsDevices.add(device);
                    }
                    if (msg.isEmpty()) {
                        int i = this.rsmsDeviceService.batchInsert(rsmsDevices);
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
    public Object export(HttpServletResponse response, RsmsDevice device) {

        if (StringUtils.isBlank(device.getExcelName())) {
            device.setExcelName("设备台账" + DateTools.getCurrentDate(new Date()));//  +".xls"
        }
        Map params = new HashMap();
        params.put("ids", device.getIds());
        List<RsmsDevice> devices = this.rsmsDeviceService.selectObjByMap(params);
        if (devices.size() > 0) {
            for (RsmsDevice rsmsDevice : devices) {
                if (rsmsDevice.getDeviceTypeId() != null && !rsmsDevice.getDeviceTypeId().equals("")) {
                    DeviceType instance = this.deviceTypeService.selectObjById(rsmsDevice.getDeviceTypeId());
                    rsmsDevice.setDeviceTypeName(instance.getName());
                }
                if (rsmsDevice.getVendorId() != null && !rsmsDevice.getVendorId().equals("")) {
                    Vendor instance = this.vendorService.selectObjById(rsmsDevice.getVendorId());
                    rsmsDevice.setVendorName(instance.getName());
                }

                if (rsmsDevice.getPlantRoomId() != null && !rsmsDevice.getPlantRoomId().equals("")) {
                    PlantRoom instance = this.plantRoomService.getObjById(rsmsDevice.getPlantRoomId());
                    rsmsDevice.setPlantRoomName(instance.getName());
                }
                if (rsmsDevice.getRackId() != null && !rsmsDevice.getRackId().equals("")) {
                    Rack instance = this.rackService.getObjById(rsmsDevice.getRackId());
                    rsmsDevice.setRackName(instance.getName());
                }
                if (rsmsDevice.getProjectId() != null && !rsmsDevice.getProjectId().equals("")) {
                    Project instance = this.projectService.selectObjById(rsmsDevice.getProjectId());
                    rsmsDevice.setProjectName(instance.getName());
                }

            }
            List<List<Object>> sheetDataList = ExcelUtils.getSheetData(devices);
            ExcelUtils.export(response, device.getExcelName(), sheetDataList);
        }
        return ResponseUtil.ok();
    }

    //    @Autowired
//    private Properties properties;
    @Value("${batchImportDeviceFileName}")
    private String batchImportDeviceFileName;
    @Value("${batchImportFilePath}")
    private String batchImportFilePath;

    @ApiOperation("下载设备批量上传模板")
    @GetMapping("/downTemp")
    public Object downTemplate(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {

        boolean flag = DownLoadFileUtil.downloadTemplate(this.batchImportFilePath, this.batchImportDeviceFileName, response);
        LOG.info("模板下载成功");
        if (flag) {
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.error();
        }
    }
}
