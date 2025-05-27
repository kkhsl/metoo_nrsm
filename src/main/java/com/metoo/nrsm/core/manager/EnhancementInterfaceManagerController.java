
package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.utils.ip.CIDRUtils;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6.Ipv6CIDRUtils;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Interface;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.*;

@Api("接口设置")
@RequestMapping("/admin/interface/enhancement")
@RestController
public class EnhancementInterfaceManagerController {

    @Autowired
    private IInterfaceService interfaceService;

    @ApiOperation("列表")
    @GetMapping({"/all"})
    public Result all() {
        List<Interface> interfaceList = this.interfaceService.selectAll();
        return ResponseUtil.ok(interfaceList);
    }

    @ApiOperation("列表")
    @RequestMapping({"/list"})
    public Result list(@RequestBody InterfaceDTO dto) {
        Page<Interface> page = this.interfaceService.selectObjConditionQuery(dto);
        return ResponseUtil.ok(new PageInfo<Interface>(page));
    }

    @ApiOperation("创建/更新")
    @PostMapping({"/save"})
    public Object save(@RequestBody Interface instance) {
        Map params = new HashMap();
        // 1.如果更新的是主接口
        if(instance.getParentId() == null){
            // 检查主接口下是否存在子接口，如果存在则不允许,报错
            List<Interface> interfaces = this.interfaceService.selectObjByParentId(instance.getId());
            if(interfaces.size() > 0){
                return ResponseUtil.badArgument("当前接口存在子接口，请先删除子接口在进行主接口配置");
            }
        }

        if (StringUtil.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument("名称不能为空");
        }else{
            params.clear();
            params.put("name", instance.getName());
            params.put("vlanNum", instance.getVlanNum());
            params.put("excludeId", instance.getId());
            if (interfaceService.selectObjByMap(params).size() >= 1) {
                return ResponseUtil.badArgument("当前接口已经存在");
            }
        }
        if(instance.getVlanNum() != null && (instance.getName() == null || "".equals(instance.getName()))){
            return ResponseUtil.badArgument("主接口不能为空");
        }
        // 校验Ipv4地址
//        if(instance.getIpv4Address() == null || StringUtil.isEmpty(instance.getIpv4Address())){
//            return ResponseUtil.badArgument("IPv4地址不能为空");
//        }
        if((instance.getIpv4Address() != null && StringUtil.isNotEmpty(instance.getIpv4Address()))){
            if(!Ipv4Util.verifyCidr(instance.getIpv4Address())){
                return ResponseUtil.badArgument("IPv4格式错误，不符合CIDR格式");
            }
            // 计算网段
            try {
                String ipv4NetworkSegment = CIDRUtils.getIPv4NetworkSegment(instance.getIpv4Address());
                // 验证网段是否已经存在
                params.clear();
                params.put("ipv4NetworkSegment", ipv4NetworkSegment);
                params.put("excludeId", instance.getId());
                if (interfaceService.selectObjByMap(params).size() >= 1) {
                    return ResponseUtil.badArgument("其他接口已配置同网段IP地址，"+instance.getIpv4Address()+"请更换IP地址");
                }else{
                    instance.setIpv4NetworkSegment(ipv4NetworkSegment);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }else{
            instance.setIpv4NetworkSegment(null);
        }
//        if(instance.getGateway4() == null || StringUtil.isEmpty(instance.getGateway4())){
//            return ResponseUtil.badArgument("IPv4网关不能为空");
//        }
        if((instance.getGateway4() != null && StringUtil.isNotEmpty(instance.getGateway4()))){
            if(!Ipv4Util.verifyIp(instance.getGateway4())){
                return ResponseUtil.badArgument("IPv4网关格式错误");
            }
            // 检查设备是否存在v4网关,一个设备只允许配置一个v4网关
            params.clear();
            params.put("gateway4NotNull", instance.getGateway4());
            params.put("excludeId", instance.getId());
            if (interfaceService.selectObjByMap(params).size() >= 1) {
                return ResponseUtil.badArgument("本设备已存在一个IPv4网关");
            }
        }
//        if(instance.getIpv6Address() == null || StringUtil.isEmpty(instance.getIpv6Address())){
//            return ResponseUtil.badArgument("IPv6地址不能为空");
//        }
        if((instance.getIpv6Address() != null && StringUtil.isNotEmpty(instance.getIpv6Address()))){
            if(!Ipv6Util.verifyCidr(instance.getIpv6Address())){
                return ResponseUtil.badArgument("IPv6格式错误，不符合CIDR格式");
            }
            try {
                String ipv6NetworkSegment = Ipv6CIDRUtils.getIPv6NetworkSegment(instance.getIpv6Address());
                // 验证网段是否已经存在
                params.clear();
                params.put("ipv6NetworkSegment", ipv6NetworkSegment);
                params.put("excludeId", instance.getId());
                if (interfaceService.selectObjByMap(params).size() >= 1) {
                    return ResponseUtil.badArgument("其他接口已配置同网段IP地址，"+instance.getIpv6Address()+" 请更换IP地址");
                }else{
                    instance.setIpv6NetworkSegment(ipv6NetworkSegment);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }else{
            instance.setIpv6NetworkSegment(null);
        }
//        if(instance.getGateway6() == null || StringUtil.isEmpty(instance.getGateway6())){
//            return ResponseUtil.badArgument("IPv6网关不能为空");
//        }
        if((instance.getGateway6() != null && StringUtil.isNotEmpty(instance.getGateway6()))){
            if(!Ipv6Util.verifyIpv6(instance.getGateway6())){
                return ResponseUtil.badArgument("IPv6网关格式错误");
            }
            params.clear();
            params.put("gateway6NotNull", instance.getGateway6());
            params.put("excludeId", instance.getId());
            if(interfaceService.selectObjByMap(params).size() >= 1){
                return ResponseUtil.badArgument("本设备已存在一个IPv6网关");
            }
        }

        if((instance.getGateway4() != null && StringUtil.isNotEmpty(instance.getGateway4())) && !Ipv4Util.verifyIp(instance.getGateway4()) && (instance.getIpv4Address() != null
                && StringUtil.isNotEmpty(instance.getIpv4Address())) && !Ipv4Util.verifyCidr(instance.getIpv4Address())){
            boolean ipv4 = this.isIPAddressMatchingGateway(instance.getIpv4Address(), instance.getGateway4());
            if(!ipv4){
                return ResponseUtil.badArgument("IPv4地址和网关不一致");
            }
        }

        if((instance.getIpv6Address() != null && StringUtil.isNotEmpty(instance.getIpv6Address())) && !Ipv6Util.verifyCidr(instance.getIpv6Address()) &&
            (instance.getGateway6() != null && StringUtil.isNotEmpty(instance.getGateway6())) && !Ipv6Util.verifyIpv6(instance.getGateway6())){
            boolean ipv6 = this.isIPAddressv6MatchingGateway(instance.getIpv6Address(), instance.getGateway6());
            if(!ipv6){
                return ResponseUtil.badArgument("IPv6地址和网关不一致");
            }
        }

        int i = this.interfaceService.save(instance);

        // 判断两次保存不一致，则修改配置，重启unbound
        return i >= 1 ? ResponseUtil.ok() : ResponseUtil.badArgument("配置失败");
    }

    @DeleteMapping({"/delete"})
    public Object delete(String ids) {
        if (ids != null && !ids.equals("")) {
            String[] var2 = ids.split(",");
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String id = var2[var4];
                Map params = new HashMap();
                params.put("id", Long.parseLong(id));
                List<Interface> instance = this.interfaceService.selectObjByMap(params);
                if (instance.size() <= 0) {
                    return ResponseUtil.badArgument();
                }

                Interface addressPool = instance.get(0);
                if(addressPool.getParentId() == null){
                    return ResponseUtil.badArgument("'" + addressPool.getName() + "'" + "删除失败, 主接口不允许删除");
                }
                try {
                    int i = this.interfaceService.delete(Long.parseLong(id));
                } catch (NumberFormatException var10) {
                    var10.printStackTrace();
                    return ResponseUtil.badArgument(addressPool.getName() + "删除失败");
                }
            }
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.badArgument();
        }
    }

    public boolean isIPAddressMatchingGateway(String ip, String gateway){
        try {
            CIDRUtils cidrUtils = new CIDRUtils(ip);
            return cidrUtils.isInRange(gateway);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean isIPAddressv6MatchingGateway(String ip, String gateway){
        try {
            Ipv6CIDRUtils ipv6CIDRUtils = new Ipv6CIDRUtils(ip);
            return ipv6CIDRUtils.isInRange(gateway);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }




}
