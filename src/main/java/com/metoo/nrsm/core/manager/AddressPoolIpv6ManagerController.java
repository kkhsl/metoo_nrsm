package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.AddressPoolIpv6DTO;
import com.metoo.nrsm.core.service.IAddressPoolIpv6Service;
import com.metoo.nrsm.core.service.ISysConfigService;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.core.utils.ip.Ipv6.TransIPv6;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.AddressPoolIpv6;
import com.metoo.nrsm.entity.SysConfig;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-09 15:12
 */
@ApiOperation("ipv6地址池")
@RequestMapping("/admin/address/pool/ipv6")
@RestController
public class AddressPoolIpv6ManagerController {

    @Autowired
    private IAddressPoolIpv6Service addressPoolIpv6Service;
    @Autowired
    private ISysConfigService sysConfigService;

    @GetMapping({"/status"})
    public Result status() {
        Map result = new HashMap();
        SysConfig sysconfig = this.sysConfigService.select();
        result.put("v6_status", sysconfig.isV6_status());
        return ResponseUtil.ok(result);
    }

    @GetMapping({"/write"})
    public Result writeDhcpd() {
        SysConfig sysconfig = this.sysConfigService.select();
        if(sysconfig.isV6_status()){
            this.addressPoolIpv6Service.write();
        }
        return ResponseUtil.ok();
    }


    @PostMapping("/list")
    public Result list(@RequestBody AddressPoolIpv6DTO dto){

        Page<AddressPoolIpv6> page = this.addressPoolIpv6Service.selectObjConditionQuery(dto);
        return ResponseUtil.ok(new PageInfo<AddressPool>(page));
    }

    @ApiOperation("创建/更新")
    @PostMapping({"/save"})
    public Object save(@RequestBody AddressPoolIpv6 instance) {
        Map params = new HashMap();
        if (StringUtil.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument("名称不能为空");
        }else{
            params.clear();
            params.put("addressPoolIpv6Id", instance.getId());
            params.put("name", instance.getName());
            List<AddressPoolIpv6> addressPools = this.addressPoolIpv6Service.selectObjByMap(params);
            if(addressPools.size() > 0){
                return ResponseUtil.badArgument("名称不能重复");
            }
        }
        if (StringUtil.isEmpty(instance.getSubnetAddresses())) {
            return ResponseUtil.badArgument("子网地址不能为空");
        }else{
            String subnet = instance.getSubnetAddresses().split("/")[0];
            if(!Ipv6Util.verifyIpv6(subnet)){
                return ResponseUtil.badArgument("子网地址格式错误");
            }
            String mask = instance.getSubnetAddresses().split("/")[1];
            String fillIpv6 = TransIPv6.getFullIPv6(subnet);
            boolean isValid = Ipv6Util.verifyCidr(fillIpv6 + "/" + mask);
            if(!isValid){
                return ResponseUtil.badArgument("错误的CIDR格式");
            }
            params.clear();
            params.put("addressPoolIpv6Id", instance.getId());
            params.put("subnetAddresses", instance.getSubnetAddresses());
            List<AddressPoolIpv6> addressPoolIpv6s = this.addressPoolIpv6Service.selectObjByMap(params);
            if(addressPoolIpv6s.size() > 0){
                return ResponseUtil.badArgument("子网地址重复");
            }
        }
        if(StringUtil.isNotEmpty(instance.getDNS())){
            List<String> dns = null;
            try {
                dns = MyStringUtils.str2list(instance.getDNS());
                if(dns.size() >= 0){
                    int i = 0;
                    for (String str : dns) {
                        i++;
                        boolean flag = Ipv6Util.verifyIpv6(str);
                        if (!flag){
                            return ResponseUtil.badArgument("第" + i + "行，dns格式错误");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int i = this.addressPoolIpv6Service.save(instance);
        return i >= 1 ? ResponseUtil.ok() : ResponseUtil.error();
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
                List<AddressPoolIpv6> addressPools = this.addressPoolIpv6Service.selectObjByMap(params);
                if (addressPools.size() <= 0) {
                    return ResponseUtil.badArgument();
                }

                AddressPoolIpv6 addressPool = addressPools.get(0);

                try {
                    int var9 = this.addressPoolIpv6Service.delete(Long.parseLong(id));
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

}
