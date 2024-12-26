package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.ssh.utils.DateUtils;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.NetworkElementDto;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.file.DownLoadFileUtil;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.poi.ExcelUtils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Api("网元管理")
@RequestMapping("/nspm/ne")
@RestController
public class NetworkElementManagerController {

    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IVendorService vendorService;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private ICredentialService credentialService;
    @Autowired
    private IRsmsDeviceService rsmsDeviceService;
    @Autowired
    private IPortService portService;
    @Autowired
    private PythonExecUtils pythonExecUtils;

    @ApiOperation("网元列表")
    @RequestMapping("/list")
    public Object list(@RequestBody(required=false) NetworkElementDto dto){
        if(dto == null){
            dto = new NetworkElementDto();
        }
        Page<NetworkElement> page = this.networkElementService.selectConditionQuery(dto);
        if(page.getResult().size() > 0){
            for(NetworkElement ne : page.getResult()) {
                if (ne.getDeviceTypeId() != null) {
                    DeviceType deviceType = deviceTypeService.selectObjById(ne.getDeviceTypeId());
                    ne.setDeviceTypeName(deviceType.getName());
                }
                if (ne.getVendorId() != null) {
                    Vendor vendor = vendorService.selectObjById(ne.getVendorId());
                    ne.setVendorName(vendor.getName());
                }
                if (ne.getCredentialId() != null) {
                    Credential credential = credentialService.getObjById(ne.getCredentialId());
                    if (credential != null) {
                        ne.setCredentialName(credential.getName());
                    }
                }
            }
            Map map = new HashMap();
            // 厂商
            List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
            map.put("vendor", vendors);
            // 设备类型
            Map parmas = new HashMap();
            parmas.put("diff", 0);
            parmas.put("orderBy", "sequence");
            parmas.put("orderType", "DESC");
            List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
            map.put("deviceType", deviceTypeList);
//            try {
//                countDownLatch.await();
//                return ResponseUtil.ok(new PageInfo<NetworkElement>(page, map));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            return ResponseUtil.ok(new PageInfo<NetworkElement>(page, map));
        }
        return  ResponseUtil.ok();
    }

    @GetMapping("/all")
    public Object all(){
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        return ResponseUtil.ok(networkElements);
    }

    @Test
    public void condition(){
        //调用工厂方法创建Optional实例
        Optional<String> name = Optional.of("Dolores");
        //创建一个空实例
        Optional empty = Optional.ofNullable(null);
        //创建一个不允许值为空的空实例
//        Optional noEmpty = Optional.of(null);

        //如果值不为null，orElse方法返回Optional实例的值。
        //如果为null，返回传入的消息。
        //输出：Dolores
        System.out.println(name.orElse("There is some value!"));
        //输出：There is no value present!
        System.out.println(empty.orElse(null));
        //抛NullPointerException
//        System.out.println(noEmpty.orElse("There is no value present!"));
    }

    @Test
    public void condition2(){
        Map params = new HashMap();
        Optional<Map> optional = Optional.ofNullable(params);
        if(optional.isPresent() && !params.isEmpty()) {
            System.out.println(0);
        }else{
            System.out.println(1);
            System.out.println(2);
        }
    }

    @GetMapping("/add")
    public Object add(){
        Map map = new HashMap();
        // 厂商
        List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
        map.put("vendor", vendors);
        // 设备类型
        Map parmas = new HashMap();
        parmas.put("diff", 0);
        parmas.put("orderBy", "sequence");
        parmas.put("orderType", "DESC");
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
        map.put("device", deviceTypeList); // 凭据列表
        List<Credential> credentials = this.credentialService.getAll();
        map.put("credential", credentials);
        return ResponseUtil.ok(map);
    }

    @GetMapping("/update")
    public Object update(Long id){
        if(id == null){
            return  ResponseUtil.badArgument();
        }
        Map map = new HashMap();

        // 厂商
        List<Vendor> vendors = this.vendorService.selectConditionQuery(null);
        map.put("vendor", vendors);
        NetworkElement networkElement = this.networkElementService.selectObjById(id);
        map.put("networkElement", networkElement);
        // 设备类型
        Map parmas = new HashMap();
        parmas.put("diff", 0);
        parmas.put("orderBy", "sequence");
        parmas.put("orderType", "DESC");
        List<DeviceType> deviceTypeList = this.deviceTypeService.selectObjByMap(parmas);
        map.put("device", deviceTypeList);  // 凭据列表
        List<Credential> credentials = this.credentialService.getAll();
        map.put("credential", credentials);
        return ResponseUtil.ok(map);
    }

    @ApiOperation("校验Ip格式")
    @GetMapping("/verify")
    public Object verify(@RequestParam(value = "ip") String ip,
                         @RequestParam(value = "id") String id){
        if (!StringUtils.isEmpty(ip)) {
            // 验证ip合法性
            boolean flag =  Ipv4Util.verifyIp(ip);
            if(!flag){
                return ResponseUtil.badArgument("ip不合法");
            }
            Map params = new HashMap();
            params.put("neId", id);
            params.put("ip", ip);
            List<NetworkElement> nes = this.networkElementService.selectObjByMap(params);
            if(nes.size() > 0){
                return ResponseUtil.badArgument("IP重复");
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument("Ip为空");
    }

    @GetMapping("/detail")
    public Object detail(@RequestParam(value = "uuid") String uuid){
        if (!StringUtils.isEmpty(uuid)) {
            NetworkElement networkElement = this.networkElementService.selectObjByUuid(uuid);
            if(networkElement != null){
                String result = null;
                try {
                    String path = Global.PYPATH + "getuptime.py";
                    String[] params = {networkElement.getIp(), networkElement.getVersion(),
                            networkElement.getCommunity()};
                    result = pythonExecUtils.exec(path, params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(StringUtil.isNotEmpty(result)){
                    String timeticks = uptime(Long.parseLong(result));
                    networkElement.setTimeticks(timeticks);
                }
                List<Port> ports = this.portService.selectObjByDeviceUuid(uuid);

                networkElement.setPorts(ports);
                return ResponseUtil.ok(networkElement);
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }


    public static String uptime(Long time){
        //获取结束时间
        Date finishTime = new Date();
        //结束时间 转为 Long 类型
        Long end = finishTime.getTime();
        // 时间差 = 结束时间 - 开始时间，这样得到的差值是毫秒级别
        long timeLag = time;
        //天
        long day=timeLag/(24*60*60*1000);
        //小时
        long hour=(timeLag/(60*60*1000) - day * 24);
        //分钟
        long minute=((timeLag/(60*1000))-day*24*60-hour*60);
        //秒，顺便说一下，1秒 = 1000毫秒
        long s=(timeLag/1000-day*24*60*60-hour*60*60-minute*60);
        System.out.println("用了 "+day+"天 "+hour+"时 "+minute+"分 "+s+"秒");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("任务结束，结束时间为："+ df.format(finishTime));
        return day+"天 "+hour+"时 "+minute+"分";
    }

    public static void main(String[] args) {
        DateUtils.formatDateTime(33815647);

            //获取结束时间
            Date finishTime = new Date();
            //结束时间 转为 Long 类型
            Long end = finishTime.getTime();
            // 时间差 = 结束时间 - 开始时间，这样得到的差值是毫秒级别
            long timeLag = 33815647;
            //天
            long day=timeLag/(24*60*60*1000);
            //小时
            long hour=(timeLag/(60*60*1000) - day * 24);
            //分钟
            long minute=((timeLag/(60*1000))-day*24*60-hour*60);
            //秒，顺便说一下，1秒 = 1000毫秒
            long s=(timeLag/1000-day*24*60*60-hour*60*60-minute*60);
            System.out.println("用了 "+day+"天 "+hour+"时 "+minute+"分 "+s+"秒");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("任务结束，结束时间为："+ df.format(finishTime));
        System.out.println( day+"天 "+hour+"时 "+minute+"分");
    }

    @PostMapping("/save")
    public Object save(@RequestBody(required=false) NetworkElement instance){
        String name = "";
        String newName = "";
        String ip = "";
        String newIp = "";
        String deviceTypeName = "";
        NetworkElement ne = null;
        if(instance.getId() != null){
            ne = this.networkElementService.selectObjById(instance.getId());
            if(ne.getDeviceName() != instance.getDeviceName()){
                name = ne.getDeviceName();
                newName = instance.getDeviceName();
            }
            if(ne.getIp() != instance.getIp()){
                ip = ne.getIp();
                newIp = instance.getIp();
            }
        }

        // 验证设备名唯一性
        Map params = new HashMap();
        if(instance.getDeviceName() == null || instance.getDeviceName().equals("")){
            return ResponseUtil.badArgument("设备名不能为空");
        }else {
            params.clear();
            params.put("neId", instance.getId());
            params.put("deviceName", instance.getDeviceName());
            List<NetworkElement> nes = this.networkElementService.selectObjByMap(params);
            if (nes.size() > 0) {
                return ResponseUtil.badArgument("设备名称重复");
            }
        }

        // 验证厂商
        Vendor vendor = this.vendorService.selectObjById(instance.getVendorId());
        if(vendor != null){
            instance.setVendorName(vendor.getName());
        }

        if(this.networkElementService.save(instance) >= 1 ? true : false){

            // 验证名称是否唯一
            if(instance.isSync_device() && instance.getDeviceName() != null
                    && !instance.getDeviceName().isEmpty()) {
                params.clear();
                params.put("id", instance.getId());
                params.put("name", instance.getDeviceName());
                List<RsmsDevice> rsmsDeviceList = this.rsmsDeviceService.selectObjByMap(params);
                if (rsmsDeviceList.size() > 0) {
                    return ResponseUtil.ok("添加成功【设备同步失败：设备已存在】");
                } else {
                    try {
                        RsmsDevice rsmsDevice = new RsmsDevice();// copy
                        rsmsDevice.setIp(instance.getIp());
                        rsmsDevice.setName(instance.getDeviceName());
                        rsmsDevice.setDeviceTypeId(instance.getDeviceTypeId());
                        rsmsDevice.setDeviceTypeName(instance.getDeviceTypeName());
                        rsmsDevice.setVendorId(instance.getVendorId());
                        rsmsDevice.setVendorName(instance.getVendorName());
                        rsmsDevice.setDescription(instance.getDescription());
                        rsmsDevice.setUuid(instance.getUuid());
                        params.clear();
                        params.put("ip", instance.getIp());
                        List<RsmsDevice> rsmsDevices = this.rsmsDeviceService.selectObjByMap(params);
                        if(rsmsDevices.size() > 0){
                            RsmsDevice obj = rsmsDevices.get(0);
                            rsmsDevice.setId(obj.getId());
                        }
                        this.rsmsDeviceService.save(rsmsDevice);
                        return ResponseUtil.ok("添加成功【设备同步成功】");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseUtil.ok("添加成功【设备同步失败】");
                    }
                }
            }

            return ResponseUtil.ok();
        }else{
            return ResponseUtil.badArgument();
        }
    }
    @DeleteMapping("/delete")
    public Object delete(String ids){
        if(ids != null && !ids.equals("")){
            User user = ShiroUserHolder.currentUser();
            for (String id : ids.split(",")){
                Map params = new HashMap();
//                params.put("userId", user.getId());
                params.put("id", Long.parseLong(id));
                List<NetworkElement> nes = this.networkElementService.selectObjByMap(params);
                if(nes.size() > 0){
                    NetworkElement ne = nes.get(0);
                    try {
                        int i = this.networkElementService.delete(Long.parseLong(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return ResponseUtil.badArgument(ne.getDeviceName() + "删除失败");
                    }
                }else{
                    return ResponseUtil.badArgument();
                }
            }

            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }

    @ApiOperation("是否开启凭证")
    @GetMapping("/isCredential")
    public Object isCredential(@RequestParam(value = "uuid") String uuid){
        NetworkElement networkElement = this.networkElementService.selectObjByUuid(uuid);
        if(networkElement != null){
//            // 验证凭据是否存在
//            Credential credential = this.credentialService.getObjById(networkElement.getCredentialId());
//            if(credential != null){
//                return ResponseUtil.ok(1);
//            }else{
//                return ResponseUtil.ok(0);

            Map map = new HashMap();
            map.put("permitConnect", networkElement.isPermitConnect());
            map.put("webUrl", networkElement.getWebUrl());
            return ResponseUtil.ok(map);
        }
        return ResponseUtil.badArgument("Uuid不存在");
    }

    @PostMapping("/import")
    public Object importExcel(@RequestPart("file")MultipartFile file) throws Exception {
        if(!file.isEmpty()){
            String fileName = file.getOriginalFilename().toLowerCase();
            String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
            if (suffix.equals("xlsx") || suffix.equals("xls")) {
                List<NetworkElement> nes = ExcelUtils.readMultipartFile(file, NetworkElement.class);
                // 校验表格数据是否符号要求
                String tips = "";
                for (NetworkElement ne : nes) {
                    if(!ne.getRowTips().isEmpty()){
                        tips = ne.getRowTips();
                        break;
                    }
                }
                if(!tips.isEmpty()){
                    return ResponseUtil.badArgument(tips);
                }
                if(nes.size() > 0){
                    String msg = "";
                    Map params = new HashMap();
                    List<NetworkElement> neList = new ArrayList<>();
                    for (int i = 0; i < nes.size(); i++) {
                        NetworkElement ne = nes.get(i);
                        if(ne.getDeviceName()  == null || ne.getDeviceName().equals("")){
                            msg = "第" + (i + 2) + "行,设备名不能为空";
                            break;
                        }else{
                            params.clear();
                            params.put("deviceName", ne.getDeviceName());
                            List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
                            if(networkElements.size() > 0){
                                msg = "第" + (i + 2) + "行, 设备已存在";
                                break;
                            }
                        }
                        // 增加外联设备，ip必填校验删除
//                        if(ne.getIp()  == null || ne.getIp().equals("")){
//                            ne.setIp("");
//                            msg = "第" + (i + 2) + "行,IP不能为空";
//                            break;
//                        }
                        if(ne.getIp() != null && !ne.getIp().equals("")){
                            boolean flag = Ipv4Util.verifyIp(ne.getIp());
                            if(flag){
                                params.clear();
                                params.put("ip", ne.getIp());
                                List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
                                if(networkElements.size() > 0){
                                    msg = "第" + (i + 2) + "行, IP已存在";
                                    break;
                                }
                            }else{
                                msg = "第" + (i + 2) + "行, IP格式错误";
                                break;
                            }
                        }
                        if(ne.getVendorName() != null && !ne.getVendorName().equals("")){
                            params.clear();
                            params.put("name", ne.getVendorName());
                            Vendor vendor = this.vendorService.selectObjByName(ne.getVendorName());
                            if(vendor == null){
                                msg = "第" + (i + 2) + "行,品牌不存在";
                                break;
                            }else{
                                ne.setVendorId(vendor.getId());
                            }
                        }else{
                            ne.setVendorName(null);
                        }
                        if(ne.getDeviceTypeName() != null && !ne.getDeviceTypeName().equals("")){
                            params.clear();
                            params.put("name", ne.getDeviceTypeName());
                            DeviceType deviceType = this.deviceTypeService.selectObjByName(ne.getDeviceTypeName());
                            if(deviceType == null){
                                msg = "第" + (i + 2) + "行,设备类型不存在";
                                break;
                            }else{
                                ne.setDeviceTypeId(deviceType.getId());
                                if(deviceType.getType() == 10){
                                    ne.setInterfaceName("Port0");
                                }
                            }
                        }else{
                            ne.setDeviceTypeName(null);
                        }
                        ne.setType(2);
                        neList.add(ne);
                    }
                    if(msg.isEmpty()){
                        // 批量插入NE
                        int i = this.networkElementService.batchInsert(neList);
                        if(i > 0){
                            return ResponseUtil.ok();
                        }else{
                            return ResponseUtil.error();
                        }
                    }else{
                        return ResponseUtil.badArgument(msg);
                    }
                }else{
                    return ResponseUtil.badArgument("文件无数据");
                }
            }else{
                return ResponseUtil.badArgument("文件格式错误，请使用标准模板上传");
            }
        }
        return ResponseUtil.badArgument("文件不存在");
    }


    @Value("${batchImportNeFileName}")
    private String batchImportNeFileName;
    @Value("${batchImportFilePath}")
    private String batchImportFilePath;

    @ApiOperation("模板下载")
    @GetMapping("/downTemp")
    public Object downTemplate(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        boolean flag = DownLoadFileUtil.downloadTemplate(this.batchImportFilePath, this.batchImportNeFileName, response);
        if(flag){
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.error();
        }
    }


    @Autowired
    private ITerminalService terminalService;

    @ApiOperation("网元|终端列表")
    @PostMapping("/terminal")
    public Object terminal(@RequestBody String[] uuids){
        if(uuids != null && uuids.length > 0){
            Map params = new HashMap();
            Map map = new HashMap();
            for (String uuid : uuids) {
                params.put("online", true);
                params.put("deviceUuid", uuid);
                params.put("deviceUuidAndDeviceTypeId", uuid);
                List<Terminal> terminals = this.terminalService.selectObjByMap(params);
                terminals.stream().forEach(e -> {
                    if(e.getDeviceTypeId() != null
                            && !e.getDeviceTypeId().equals("")){
                        DeviceType deviceType = this.deviceTypeService.selectObjById(e.getDeviceTypeId());
                        e.setDeviceTypeName(deviceType.getName());
                        e.setDeviceTypeUuid(deviceType.getUuid());
                    }
                });
                map.put(uuid, terminals);
            }
            return ResponseUtil.ok(map);
        }
        return ResponseUtil.ok();
    }



    @SneakyThrows
    @ApiOperation("/允许连接设备")
    @PostMapping("/permit/connect")
    public Object condition(@RequestBody Map params) {
        Optional<Map> optional = Optional.ofNullable(params);
        if(optional.isPresent() && !params.isEmpty()){
            List list = this.networkElementService.selectObjByMap(params);
            return ResponseUtil.ok(list);
        }
        throw new MissingServletRequestParameterException("","");
    }

}
