
package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.annotation.OperationLogAnno;
import com.metoo.nrsm.core.config.annotation.OperationType;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.service.IAddressPoolService;
import com.metoo.nrsm.core.utils.ip.IpV4Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.AddressPool;
import io.swagger.annotations.ApiOperation;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public AddressPoolManagerController() {
    }

    @GetMapping({"/write"})
    public Result write1() {
        this.addressPoolService.write();
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
    public Object save(@RequestBody AddressPool addressPool) {
        if (StringUtil.isEmpty(addressPool.getName())) {
            return ResponseUtil.badArgument("名称不能为空");
        }
        if (StringUtil.isEmpty(addressPool.getSubnetAddresses())) {
            return ResponseUtil.badArgument("子网地址不能为空");
        }
        if(StringUtil.isEmpty(addressPool.getSubnetAddresses())){
            return ResponseUtil.badArgument("以CIDR格式输入子网");
        }else{
            if(!IpV4Util.verifyCidr(addressPool.getSubnetAddresses())){
                return ResponseUtil.badArgument("错误的CIDR格式");
            }
        }
        String subnetAddress = addressPool.getSubnetAddresses();
        String networkAddress = IpV4Util.getNetwork(subnetAddress.substring(0, subnetAddress.indexOf("/")),
                IpV4Util.getMaskByMaskBit(Integer.parseInt(subnetAddress.substring(subnetAddress.indexOf("/") + 1))));
        if(!subnetAddress.substring(0, subnetAddress.indexOf("/")).equals(networkAddress)){
            return ResponseUtil.badArgument("子网地址错误");
        }
        // 验证地址池范围格式是否正确
        if(StringUtil.isNotEmpty(addressPool.getAddressPoolRange())){
            if(addressPool.getAddressPoolRange().contains("-")){
                String addressPoolRange = addressPool.getAddressPoolRange();
                addressPoolRange = addressPoolRange.replaceAll("\\s*|\r|\n|\t","");
                String start = addressPoolRange.substring(0, addressPoolRange.indexOf("-"));
                String end = addressPoolRange.substring(addressPoolRange.indexOf("-") + 1);
                boolean startBN =  IpV4Util.verifyIp(start);
                boolean endBN =  IpV4Util.verifyIp(end);
                if(!startBN){
                    return ResponseUtil.badArgument(start + "格式错误");
                }
                if(!endBN){
                    return ResponseUtil.badArgument(end + "格式错误");
                }
                int rangeBN = IpV4Util.compareIP(end, start);
                if(rangeBN < 0){
                    return ResponseUtil.badArgument("范围格式错误");
                }

                boolean startIsNet = IpV4Util.ipIsInNet(start, addressPool.getSubnetAddresses());
                boolean endIsNet = IpV4Util.ipIsInNet(end, addressPool.getSubnetAddresses());
                if(!startIsNet){
                    return ResponseUtil.badArgument(start + "不属于子网地址");
                }
                if(!endIsNet){
                    return ResponseUtil.badArgument(end + "不属于子网地址");
                }
            }
        }
        int i = this.addressPoolService.save(addressPool);
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
        AddressPool addressPool1 = new AddressPool();
        addressPool1.setAddTime(new Date());
        addressPool1.setName("公司内网1");
        addressPool1.setSubnetAddresses("192.168.5.101/24");
        addressPool1.setDefaultGateway("192.168.5.101");
        addressPool1.setDNS("192.168.5.101");
        System.out.println(addressPool1.toString());
        AddressPool addressPool2 = new AddressPool();
        addressPool2.setName("公司内网2");
        addressPool2.setDefaultGateway("192.168.5.101");
        addressPool2.setDNS("192.168.5.101");
        addressPool2.setSubnetAddresses("192.168.5.101/24");
        List<AddressPool> list = new ArrayList();
        list.add(addressPool1);
        list.add(addressPool2);
        List filteredEntities = (List)list.stream().filter((e) -> {
            return e != null && !e.getName().isEmpty();
        }).collect(Collectors.toList());

        try {
            FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp.txt");
            Iterator var6 = filteredEntities.iterator();

            while(var6.hasNext()) {
                AddressPool entity = (AddressPool)var6.next();
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
}
