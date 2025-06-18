
package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.annotation.OperationLogAnno;
import com.metoo.nrsm.core.config.annotation.OperationType;
import com.metoo.nrsm.core.config.aop.idempotent.NotRepeat;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.service.IAddressPoolService;
import com.metoo.nrsm.core.service.ISysConfigService;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.AddressPoolIpv6VO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.SysConfig;
import io.swagger.annotations.ApiOperation;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping({"/admin/address/pool"})
@RestController
public class AddressPoolManagerController {
    @Autowired
    private IAddressPoolService addressPoolService;
    @Autowired
    private ISysConfigService sysConfigService;

    @GetMapping({"/status"})
    public Result status() {
        Map result = new HashMap();
        SysConfig sysconfig = this.sysConfigService.select();
        result.put("v4_status", sysconfig.isV4_status());
        return ResponseUtil.ok(result);
    }

    /**
     * 幂等：避免多次提交，导致多次生成配置文件，以及重启
     * @return
     */
    @NotRepeat
    @GetMapping({"/write"})
    public Result writeDhcpd() {
        SysConfig sysconfig = this.sysConfigService.select();
        if(sysconfig.isV4_status()){
            this.addressPoolService.write();
        }
        return ResponseUtil.ok();
    }

    @ApiOperation("列表")
    @RequestMapping({"/list"})
    public Result list(@RequestBody AddressPoolDTO dto) {
        Page<AddressPool> page = this.addressPoolService.selectObjConditionQuery(dto);
        return ResponseUtil.ok(new PageInfo<AddressPool>(page));
//        return page.getResult().size() > 0 ? ResponseUtil.ok(new PageInfo(page)) : ResponseUtil.ok();
    }

    @OperationLogAnno(operationType= OperationType.CREATE, name = "地址池")
    @ApiOperation("创建/更新")
    @PostMapping({"/save"})
    public Object save(@RequestBody AddressPool instance) {
        Map params = new HashMap();
        if (StringUtil.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument("名称不能为空");
        }else{
            params.clear();
            params.put("addressPoolId", instance.getId());
            params.put("name", instance.getName());
            List<AddressPool> addressPools = this.addressPoolService.selectObjByMap(params);
            if(addressPools.size() > 0){
                return ResponseUtil.badArgument("名称不能重复");
            }
        }
        if(StringUtil.isEmpty(instance.getSubnetAddresses())){
            return ResponseUtil.badArgument("子网地址不能为空");
        }else{
            String subnet = instance.getSubnetAddresses().split("/")[0];
            if(!Ipv4Util.verifyIp(subnet)){
                return ResponseUtil.badArgument("子网地址格式错误");
            }
            if(!Ipv4Util.verifyCidr(instance.getSubnetAddresses())){
                return ResponseUtil.badArgument("错误的CIDR格式");
            }
            params.clear();
            params.put("addressPoolId", instance.getId());
            params.put("subnetAddresses", instance.getSubnetAddresses());
            List<AddressPool> addressPools = this.addressPoolService.selectObjByMap(params);
            if(addressPools.size() > 0){
                return ResponseUtil.badArgument("子网地址不能重复");
            }
        }
        String subnetAddress = instance.getSubnetAddresses();
        String networkAddress = Ipv4Util.getNetwork(subnetAddress.substring(0, subnetAddress.indexOf("/")),
                Ipv4Util.getMaskByMaskBit(Integer.parseInt(subnetAddress.substring(subnetAddress.indexOf("/") + 1))));
        if(!subnetAddress.substring(0, subnetAddress.indexOf("/")).equals(networkAddress)){
            return ResponseUtil.badArgument("子网地址错误");
        }
        if(StringUtil.isNotEmpty(instance.getDefaultGateway())){
            boolean flag = Ipv4Util.ipIsInNet(instance.getDefaultGateway(), instance.getSubnetAddresses());
            if(!flag){
                return ResponseUtil.badArgument("默认网关与子网地址不在同一网段中");
            }
        }
        // 验证地址池范围格式是否正确
        if(StringUtil.isNotEmpty(instance.getAddressPoolRange())){
            if(instance.getAddressPoolRange().contains("-")){
                String addressPoolRange = instance.getAddressPoolRange();
                addressPoolRange = addressPoolRange.replaceAll("\\s*|\r|\n|\t","");
                String start = addressPoolRange.substring(0, addressPoolRange.indexOf("-"));
                String end = addressPoolRange.substring(addressPoolRange.indexOf("-") + 1);
                boolean startBN =  Ipv4Util.verifyIp(start);
                boolean endBN =  Ipv4Util.verifyIp(end);
                if(!startBN){
                    return ResponseUtil.badArgument(start + "格式错误");
                }
                if(!endBN){
                    return ResponseUtil.badArgument(end + "格式错误");
                }
                int rangeBN = Ipv4Util.compareIP(end, start);
                if(rangeBN < 0){
                    return ResponseUtil.badArgument("范围格式错误");
                }

                boolean startIsNet = Ipv4Util.ipIsInNet(start, instance.getSubnetAddresses());
                boolean endIsNet = Ipv4Util.ipIsInNet(end, instance.getSubnetAddresses());
                if(!startIsNet){
                    return ResponseUtil.badArgument(start + "不属于子网地址");
                }
                if(!endIsNet){
                    return ResponseUtil.badArgument(end + "不属于子网地址");
                }
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
                        boolean flag = Ipv4Util.verifyIp(str);
                        if (!flag){
                            return ResponseUtil.badArgument("第" + i + "行，dns格式错误");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int i = this.addressPoolService.save(instance);
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
                List<AddressPool> addressPools = this.addressPoolService.selectObjByMap(params);
                if (addressPools.size() <= 0) {
                    return ResponseUtil.badArgument();
                }

                AddressPool addressPool = (AddressPool)addressPools.get(0);

                try {
                    int var9 = this.addressPoolService.delete(Long.parseLong(id));
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

    @Test
    public void write() {
        AddressPoolVO addressPool1 = new AddressPoolVO();
        addressPool1.setName("公司内网1");
        addressPool1.setSubnetAddresses("192.168.5.101/24");
        addressPool1.setDefaultGateway("192.168.5.101");
        addressPool1.setDNS("192.168.5.101");
        System.out.println(addressPool1.toString());
        AddressPoolVO addressPool2 = new AddressPoolVO();
        addressPool2.setName("公司内网2");
        addressPool2.setDefaultGateway("192.168.5.101");
        addressPool2.setDNS("192.168.5.101");
        addressPool2.setSubnetAddresses("192.168.5.101/24");
        List<AddressPoolVO> list = new ArrayList();
        list.add(addressPool1);
        list.add(addressPool2);
        List filteredEntities = (List)list.stream().filter((e) -> {
            return e != null && !e.getName().isEmpty();
        }).collect(Collectors.toList());

        try {
            FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp.txt");
            Iterator var6 = filteredEntities.iterator();

            while(var6.hasNext()) {
                AddressPoolVO entity = (AddressPoolVO)var6.next();
                LinkedHashMap<String, Object> map = new LinkedHashMap();
                Class<?> clazz = addressPool1.getClass();
                Field[] var10 = clazz.getDeclaredFields();
                int var11 = var10.length;

                for(int var12 = 0; var12 < var11; ++var12) {
                    Field field = var10[var12];
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    if (!fieldName.equals("id") && !fieldName.equals("addTime")) {
                        Object value = field.get(addressPool1);
                        map.put(fieldName, value);
                    }
                }

                Collection<Object> values = map.values();
                Iterator var19 = values.iterator();

                while(var19.hasNext()) {
                    Object value = var19.next();
                    if (value != null) {
                        System.out.println(value);
                        byte[] bytes = value.toString().getBytes();
                        fos.write(bytes);
                    }
                }

                fos.write("}\n\n".getBytes());
            }

            fos.close();
        } catch (IOException var16) {
            var16.printStackTrace();
        } catch (IllegalAccessException var17) {
            var17.printStackTrace();
        }

    }


    @Test
    public void writeDhcp6() {

//        AddressPoolIpv6VO default_ipv6 = new AddressPoolIpv6VO();
//        default_ipv6.getDefault();
//
//        AddressPoolIpv6VO addressPoolIpv6_1 = new AddressPoolIpv6VO();
//
//        addressPoolIpv6_1.setName("公司内网1");
//        addressPoolIpv6_1.setSubnetAddresses("240e:380:11d:2::/64");
//        addressPoolIpv6_1.setAddressPoolRange("240e:380:11d:2::667 240e:380:11d:2::ffff");
//        addressPoolIpv6_1.setDNS("2400:3200::1");
//
//        AddressPoolIpv6VO addressPoolIpv6_2 = new AddressPoolIpv6VO();
//        addressPoolIpv6_2.setName("公司内网2");
//        addressPoolIpv6_2.setSubnetAddresses("240e:380:11d:22::/64");
//        addressPoolIpv6_2.setAddressPoolRange("240e:380:11d:2::6672 240e:380:11d:2::ffff2");
//        addressPoolIpv6_2.setDNS("2400:3200::12");

        AddressPoolIpv6VO addressPoolIpv6_3 = new AddressPoolIpv6VO();
        addressPoolIpv6_3.setName("公司内网3");
        addressPoolIpv6_3.setSubnetAddresses("240e:380:11d:22::/64");
        addressPoolIpv6_3.setAddressPoolRange("240e:380:11d:2::6672 240e:380:11d:2::ffff2");
        addressPoolIpv6_3.setDNS("2400:3200::12");

        List<AddressPoolIpv6VO> list = new ArrayList();
//        list.add(default_ipv6);
//        list.add(addressPoolIpv6_1);
//        list.add(addressPoolIpv6_2);
//        list.add(addressPoolIpv6_2);
        list.add(addressPoolIpv6_3);
        List filteredEntities = list.stream().filter((e) -> {
            return e != null; // && !e.getName().isEmpty()
        }).collect(Collectors.toList());

        try {
            FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp6.txt", true);
            Iterator var6 = filteredEntities.iterator();

            while(var6.hasNext()) {
                AddressPoolIpv6VO entity = (AddressPoolIpv6VO)var6.next();
                LinkedHashMap<String, Object> map = new LinkedHashMap();
//                    Class<?> clazz = AddressPoolIpv6.class;
//                Class<?> clazz = addressPoolIpv6_1.getClass();
                Class<?> clazz = entity.getClass();
                Field[] var10 = clazz.getDeclaredFields();
                int var11 = var10.length;

                for(int var12 = 0; var12 < var11; ++var12) {
                    Field field = var10[var12];
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    if (!fieldName.equals("id") && !fieldName.equals("addTime")) {
                        Object value = field.get(entity);
                        map.put(fieldName, value);
                    }
                }

                Collection<Object> values = map.values();
                Iterator var19 = values.iterator();

                while(var19.hasNext()) {
                    Object value = var19.next();
                    if (value != null) {
                        System.out.println(value);
                        byte[] bytes = value.toString().getBytes();
                        fos.write(bytes);
                    }
                }
                if(StringUtils.isNotEmpty(entity.getName())){
                    fos.write("}\n\n".getBytes());
                }else{
                    fos.write("\n".getBytes());
                }
            }
            fos.close();
        } catch (IOException var16) {
            var16.printStackTrace();
        } catch (IllegalAccessException var17) {
            var17.printStackTrace();
        }

    }
}
