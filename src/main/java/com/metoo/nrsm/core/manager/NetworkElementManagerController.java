package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.NetworkElementDto;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.IVendorService;
import com.metoo.nrsm.core.utils.ip.IpV4Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.nspm.DeviceType;
import com.metoo.nrsm.entity.nspm.NetworkElement;
import com.metoo.nrsm.entity.nspm.User;
import com.metoo.nrsm.entity.nspm.Vendor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
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


    @ApiOperation("网元列表")
    @RequestMapping("/list")
    public Object list(@RequestBody(required=false) NetworkElementDto dto){
        if(dto == null){
            dto = new NetworkElementDto();
        }
        Page<NetworkElement> page = this.networkElementService.selectConditionQuery(dto);
        if(page.getResult().size() > 0){
            for(NetworkElement ne : page.getResult()){
                if(ne.getDeviceTypeId() != null){
                    DeviceType deviceType = this.deviceTypeService.selectObjById(ne.getDeviceTypeId());
                    ne.setDeviceTypeName(deviceType.getName());
                }
                if(ne.getVendorId() != null){
                    Vendor vendor = this.vendorService.selectObjById(ne.getVendorId());
                    ne.setVendorName(vendor.getName());
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
            map.put("device", deviceTypeList);

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
        map.put("device", deviceTypeList);
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
        map.put("device", deviceTypeList);
        return ResponseUtil.ok(map);
    }


    @ApiOperation("校验Ip格式")
    @GetMapping("/verify")
    public Object verify(@RequestParam(value = "ip") String ip,
                         @RequestParam(value = "id") String id){
        if (!StringUtils.isEmpty(ip)) {
            // 验证ip合法性
            boolean flag =  IpV4Util.verifyIp(ip);
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

}
