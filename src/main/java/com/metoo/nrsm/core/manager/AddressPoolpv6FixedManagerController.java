package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.annotation.OperationLogAnno;
import com.metoo.nrsm.core.config.annotation.OperationType;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.AddressPoolV6FixedDTO;
import com.metoo.nrsm.core.service.IAddressPoolV6FixedService;
import com.metoo.nrsm.core.service.IAddressPoolV6FixedService;
import com.metoo.nrsm.core.utils.ip.Ipv6.IPv6SubnetCheck;
import com.metoo.nrsm.core.utils.ip.Ipv6.Ipv6Utils;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.core.vo.AddressPoolV6FixedVO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.AddressPoolIpv6;
import com.metoo.nrsm.entity.AddressPoolV6Fixed;
import io.swagger.annotations.ApiOperation;
import org.apache.tomcat.util.net.IPv6Utils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-10 9:59
 */
@RequestMapping("/admin/address/pool/ipv6/fixed")
@RestController
public class AddressPoolpv6FixedManagerController {

    @Autowired
    private IAddressPoolV6FixedService addressPoolV6FixedService;

    @PostMapping("/list")
    public Result list(@RequestBody AddressPoolV6FixedDTO dto){
        Page<AddressPoolV6Fixed> page = this.addressPoolV6FixedService.selectObjConditionQuery(dto);
        return ResponseUtil.ok(new PageInfo<AddressPoolV6Fixed>(page));
    }

    @OperationLogAnno(operationType= OperationType.CREATE, name = "地址池")
    @ApiOperation("创建/更新")
    @PostMapping({"/save"})
    public Object save(@RequestBody AddressPoolV6Fixed instance) {
        Map params = new HashMap();
        if (StringUtil.isEmpty(instance.getHost())) {
            return ResponseUtil.badArgument("名称不能为空");
        }else{
            params.clear();
            params.put("addressPoolIpv6FixedId", instance.getId());
            params.put("host", instance.getHost());
            List<AddressPoolV6Fixed> addressPools = this.addressPoolV6FixedService.selectObjByMap(params);
            if(addressPools.size() > 0){
                return ResponseUtil.badArgument("名称不能重复");
            }
        }
        if (StringUtil.isEmpty(instance.getFixed_address6())) {
            return ResponseUtil.badArgument("Ip地址不能为空");
        }else{
            if(!Ipv6Util.verifyIpv6(instance.getFixed_address6())){
                return ResponseUtil.badArgument("Ip地址格式错误");
            }
            params.clear();
            params.put("addressPoolIpv6FixedId", instance.getId());
            params.put("fixed_address6", instance.getFixed_address6());
            List<AddressPoolV6Fixed> addressPoolIpv6s = this.addressPoolV6FixedService.selectObjByMap(params);
            if(addressPoolIpv6s.size() > 0){
                return ResponseUtil.badArgument("Ip地址重复");
            }
        }

        if (StringUtil.isEmpty(instance.getHost_identifier_option_dhcp6_client_id())) {
            return ResponseUtil.badArgument("设备标识不能为空");
        }else{
            params.clear();
            params.put("addressPoolIpv6FixedId", instance.getId());
            params.put("host_identifier_option_dhcp6_client_id", instance.getHost_identifier_option_dhcp6_client_id());
            List<AddressPoolV6Fixed> addressPoolIpv6s = this.addressPoolV6FixedService.selectObjByMap(params);
            if(addressPoolIpv6s.size() > 0){
                return ResponseUtil.badArgument("设备标识重复");
            }
        }

//        if(StringUtil.isNotEmpty(addressPoolFixed.getFixed_address6())){
//            String networkAddress = Ipv6Utils.getIpv6networkAddress(addressPoolFixed.getFixed_address6())
//        }
        int i = this.addressPoolV6FixedService.save(instance);
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
                List<AddressPoolV6Fixed> addressPoolFixeds = this.addressPoolV6FixedService.selectObjByMap(params);
                if (addressPoolFixeds.size() <= 0) {
                    return ResponseUtil.badArgument();
                }
                AddressPoolV6Fixed addressPool = addressPoolFixeds.get(0);

                try {
                    int var9 = this.addressPoolV6FixedService.delete(Long.parseLong(id));
                } catch (NumberFormatException var10) {
                    var10.printStackTrace();
                    return ResponseUtil.badArgument(addressPool.getHost() + "删除失败");
                }
            }
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.badArgument();
        }
    }

    // 测试并发写
    @Test
    public void write() {
        AddressPoolV6FixedVO addressPoolFixedVO_1 = new AddressPoolV6FixedVO();
        addressPoolFixedVO_1.setHost("TestAbstrack-1");
        addressPoolFixedVO_1.setHost_identifier_option_dhcp6_client_id("00:01:00:01:4a:1f:ba:e3:60:b9:1f:01:23:45");
        addressPoolFixedVO_1.setFixed_address6("2001:db8:0:1::127");

        AddressPoolV6FixedVO addressPoolFixedVO_2 = new AddressPoolV6FixedVO();
        addressPoolFixedVO_2.setHost("TestAbstrack-2");
        addressPoolFixedVO_2.setHost_identifier_option_dhcp6_client_id("00:01:00:01:4a:1f:ba:e3:60:b9:1f:01:23:45");
        addressPoolFixedVO_2.setFixed_address6("2001:db8:0:1::127");

        List<AddressPoolV6FixedVO> list = new ArrayList();
        list.add(addressPoolFixedVO_1);
        list.add(addressPoolFixedVO_2);

        List filteredEntities = (List)list.stream().filter((e) -> {
            return e != null && !e.getHost().isEmpty();
        }).collect(Collectors.toList());

        try {
            FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp_fixed6.txt");
            Iterator var6 = filteredEntities.iterator();

            while(var6.hasNext()) {
                AddressPoolV6FixedVO entity = (AddressPoolV6FixedVO)var6.next();
                LinkedHashMap<String, Object> map = new LinkedHashMap();
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
                        value = "       " + value;
                        byte[] bytes = value.toString().getBytes();
                        fos.write(bytes);
                    }
                }
                fos.write("        }\n\n".getBytes());
            }
            fos.close();
        } catch (IOException var16) {
            var16.printStackTrace();
        } catch (IllegalAccessException var17) {
            var17.printStackTrace();
        }

    }

    private static Lock lock = new ReentrantLock();

    public static void write2(List<AddressPoolV6FixedVO> list) {

        lock.lock();
        try {
            List filteredEntities = (List)list.stream().filter((e) -> {
                return e != null && !e.getHost().isEmpty();
            }).collect(Collectors.toList());

            try {
                FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp_fixed.txt");
                Iterator var6 = filteredEntities.iterator();

                fos.write("group { \n\n".getBytes());
                while(var6.hasNext()) {
                    AddressPoolV6FixedVO entity = (AddressPoolV6FixedVO)var6.next();
                    LinkedHashMap<String, Object> map = new LinkedHashMap();
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
                            value = "       " + value;
                            byte[] bytes = value.toString().getBytes();
                            fos.write(bytes);
                        }
                    }
                    fos.write("        }\n\n".getBytes());
                }

                fos.write("}\n\n".getBytes());

                fos.close();
            } catch (IOException var16) {
                var16.printStackTrace();
            } catch (IllegalAccessException var17) {
                var17.printStackTrace();
            }
        } finally {
            lock.unlock();
        }

    }


}
