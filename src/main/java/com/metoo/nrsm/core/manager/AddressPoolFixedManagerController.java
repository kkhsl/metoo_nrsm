package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.annotation.OperationLogAnno;
import com.metoo.nrsm.core.config.annotation.OperationType;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.AddressPoolFixedDTO;
import com.metoo.nrsm.core.service.IAddressPoolFixedService;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.core.vo.AddressPoolFixedVO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.AddressPoolFixed;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping("/admin/address/pool/fixed")
@RestController
public class AddressPoolFixedManagerController {

    @Autowired
    private IAddressPoolFixedService addressPoolFixedService;

    @PostMapping("/list")
    public Result list(@RequestBody AddressPoolFixedDTO dto){
        Page<AddressPoolFixed> page = this.addressPoolFixedService.selectObjConditionQuery(dto);
        return ResponseUtil.ok(new PageInfo<AddressPoolFixed>(page));
    }

    @OperationLogAnno(operationType= OperationType.CREATE, name = "地址池")
    @ApiOperation("创建/更新")
    @PostMapping({"/save"})
    public Object save(@RequestBody AddressPoolFixed instance) {
        if (StringUtil.isEmpty(instance.getHost())) {
            return ResponseUtil.badArgument("名称不能为空");
        }else{
            Map params = new HashMap();
            params.put("addressPoolFixedId", instance.getId());
            params.put("host", instance.getHost());
            List<AddressPoolFixed> addressPoolFixeds = this.addressPoolFixedService.selectObjByMap(params);
            if(addressPoolFixeds.size() > 0){
                return ResponseUtil.badArgument("名称不能重复");
            }
            if(Ipv6Util.verifyIpv6(instance.getHost())){
                return ResponseUtil.badArgument("名称不能使用Ip地址");
            }
        }
        if (StringUtil.isEmpty(instance.getHardware_ethernet())) {
            return ResponseUtil.badArgument("设备标识不能为空");
        }
        if (StringUtil.isEmpty(instance.getFixed_address())) {
            return ResponseUtil.badArgument("Ip地址不能为空");
        }else{
            if(!Ipv4Util.verifyIp(instance.getFixed_address())){
                return ResponseUtil.badArgument("Ip地址格式错误");
            }
            Map params = new HashMap();
            params.put("addressPoolFixedId", instance.getId());
            params.put("fixed_address", instance.getFixed_address());
            List<AddressPoolFixed> addressPoolFixeds = this.addressPoolFixedService.selectObjByMap(params);
            if(addressPoolFixeds.size() > 0){
                return ResponseUtil.badArgument("Ip地址不能重复");
            }
        }

        int i = this.addressPoolFixedService.save(instance);
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
                List<AddressPoolFixed> addressPoolFixeds = this.addressPoolFixedService.selectObjByMap(params);
                if (addressPoolFixeds.size() <= 0) {
                    return ResponseUtil.badArgument();
                }
                AddressPoolFixed addressPool = addressPoolFixeds.get(0);

                try {
                    int var9 = this.addressPoolFixedService.delete(Long.parseLong(id));
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
        AddressPoolFixedVO addressPoolFixedVO_1 = new AddressPoolFixedVO();
        addressPoolFixedVO_1.setHost("test-1");
        addressPoolFixedVO_1.setHardware_ethernet("58:20:59:8b:f4:94");
        addressPoolFixedVO_1.setFixed_address("192.168.5.5");

        AddressPoolFixedVO addressPoolFixedVO_2 = new AddressPoolFixedVO();
        addressPoolFixedVO_2.setHost("test-2");
        addressPoolFixedVO_2.setHardware_ethernet("58:20:59:8b:f4:94");
        addressPoolFixedVO_2.setFixed_address("192.168.5.5");

        List<AddressPoolFixedVO> list = new ArrayList();
        list.add(addressPoolFixedVO_1);
        list.add(addressPoolFixedVO_2);

        List filteredEntities = (List)list.stream().filter((e) -> {
            return e != null && !e.getHost().isEmpty();
        }).collect(Collectors.toList());

        try {
            FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp_fixed.txt");
            Iterator var6 = filteredEntities.iterator();

            fos.write("group { \n\n".getBytes());
            while(var6.hasNext()) {
                AddressPoolFixedVO entity = (AddressPoolFixedVO)var6.next();
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

    }

    private static Lock lock = new ReentrantLock();

    public static void write2(List<AddressPoolFixedVO> list) {

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
                    AddressPoolFixedVO entity = (AddressPoolFixedVO)var6.next();
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
