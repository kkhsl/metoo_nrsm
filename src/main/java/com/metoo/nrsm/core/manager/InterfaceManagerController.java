
package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.service.IUnboundService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.ip.CIDRUtils;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6CIDRUtils;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.utils.unbound.RestartUnboundUtils;
import com.metoo.nrsm.core.utils.unbound.UnboundConfUtil;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Interface;
import com.metoo.nrsm.entity.Unbound;
import com.metoo.nrsm.entity.Vlans;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.*;

@Api("接口设置")
@RequestMapping("/admin/interface")
@RestController
public class InterfaceManagerController {
    
    @Autowired
    private IInterfaceService interfaceService;
    @Autowired
    private PythonExecUtils pythonExecUtils;

    @Autowired
    private IUnboundService unboundService;


    @GetMapping({"/restart"})
    public boolean restart()  {
        boolean flag = RestartUnboundUtils.restartUnboundService();
        return flag;
    }
    @GetMapping({"/writeUnbound"})
    public String writeUnbound() throws Exception {
        Unbound unbound = this.unboundService.selectObjByOne(Collections.EMPTY_MAP);

        Set<String> list = new HashSet<>();
//        String path = Global.PYPATH + "getnetintf.py";
//        String result = pythonExecUtils.exec(path);
        String result = SNMPv2Request.getNetworkInterfaces();
        if(!"".equals(result)){
            LinkedHashMap<String, Object> map = JSONObject.parseObject(result, LinkedHashMap.class);
            for (String key : map.keySet()) {
                Interface inteface = JSONObject.parseObject(JSONObject.toJSONString(map.get(key)), Interface.class);
                if(inteface.getIsup().equals("up")){
                    if(Ipv6Util.verifyCidr(inteface.getIpv6address())){
                        list.add(inteface.getIpv6address());
                    }
                }
            }
            unbound.setInterfaces(list);
            boolean flag = UnboundConfUtil.updateInterfaceFile(Global.unboundPath, unbound);
            if (!flag) {
                return "文件更新失败";
            }else{
                boolean result1 = RestartUnboundUtils.restartUnboundService();
                return "文件更新成功";
            }
        }
        return "文件更新成功 None";
    }

    @GetMapping({"/write"})
    public Result write() {
        List<Interface> list = new ArrayList<>();

        try {
            String[] args = new String[] {
                    "python", "E:\\python\\project\\djangoProject\\app01\\test.py"};

            Process proc = Runtime.getRuntime().exec(args);// 执行py文件

            StringBuffer sb = new StringBuffer();
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(),"gb2312"));//解决中文乱码，参数可传中文
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }

            Map<String, Object> map = JSONObject.parseObject(sb.toString(), Map.class);

            for (String key : map.keySet()) {
                System.out.println("key: " + key + " value: " + map.get(key));
                Interface inteface = JSONObject.parseObject(JSONObject.toJSONString(map.get(key)), Interface.class);
                inteface.setName(key);
                list.add(inteface);
            }


//            list.forEach(e -> {
//                this.interfaceService.save(e);
//            });


            in.close();
            proc.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseUtil.ok(list);


    }
    @ApiOperation("网络-接口列表")
    @GetMapping({"/info"})
    public Result info() {
        List<Interface> list = new ArrayList<>();
//        String path = "/opt/nrsm/py/getnetintf.py";
//        String result = pythonExecUtils.exec(path);
        String result = SNMPv2Request.getNetworkInterfaces();
        if(!"".equals(result)){
            LinkedHashMap<String, Object> map = JSONObject.parseObject(result, LinkedHashMap.class);
            for (String key : map.keySet()) {
                Interface inteface = JSONObject.parseObject(JSONObject.toJSONString(map.get(key)), Interface.class);
                inteface.setName(key);
                list.add(inteface);
            }
            return ResponseUtil.ok(list);
        }
        return ResponseUtil.ok();
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
        if (StringUtil.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument("名称不能为空");
        }

        // 校验Ipv4地址
        if(!Ipv4Util.verifyCidr(instance.getIpv4address())){
            return ResponseUtil.badArgument("Ipv4格式错误，不符合CIDR格式");
        }
        if(!Ipv4Util.verifyIp(instance.getGateway4())){
            return ResponseUtil.badArgument("Ipv4网关格式错误");
        }

        if(!Ipv6Util.verifyCidr(instance.getIpv6address())){
            return ResponseUtil.badArgument("Ipv6格式错误，不符合CIDR格式");
        }

        if(!Ipv6Util.verifyIpv6(instance.getGateway6())){
            return ResponseUtil.badArgument("Ipv6网关格式错误");
        }

        boolean ipv4 = this.isIPAddressMatchingGateway(instance.getIpv4address(), instance.getGateway4());

        if(!ipv4){
            return ResponseUtil.badArgument("Ipv4地址和网关不一致");
        }

        boolean ipv6 = this.isIPAddressv6MatchingGateway(instance.getIpv6address(), instance.getGateway6());

        if(!ipv6){
            return ResponseUtil.badArgument("Ipv6地址和网关不一致");
        }

        int i = this.interfaceService.save(instance);

        // 判断两次保存不一致，则修改配置，重启unbound

        return i >= 1 ? ResponseUtil.ok() : ResponseUtil.badArgument("配置失败");
    }

    @ApiOperation("编辑")
    @PostMapping({"/modify/ip"})
    public Object modifyIp(@RequestBody Interface instance) {
        if (StringUtil.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument ("网络接口不能为空");
        }
        // 校验Ipv4地址
        if(StringUtils.isNotEmpty(instance.getIpv4address()) &&!Ipv4Util.verifyCidr(instance.getIpv4address())){
            return ResponseUtil.badArgument("Ipv4格式错误，不符合CIDR格式");
        }
        if(StringUtils.isNotEmpty(instance.getGateway4()) && !Ipv4Util.verifyIp(instance.getGateway4())){
            return ResponseUtil.badArgument("Ipv4网关格式错误");
        }

        if(StringUtils.isNotEmpty(instance.getIpv6address()) && !Ipv6Util.verifyCidr(instance.getIpv6address())){
            return ResponseUtil.badArgument("Ipv6格式错误，不符合CIDR格式");
        }

        if(StringUtils.isNotEmpty(instance.getGateway6()) &&!Ipv6Util.verifyIpv6(instance.getGateway6())){
            return ResponseUtil.badArgument("Ipv6网关格式错误");
        }

        if(StringUtils.isNotEmpty(instance.getIpv4address()) && StringUtils.isNotEmpty(instance.getGateway4())){
            boolean ipv4 = this.isIPAddressMatchingGateway(instance.getIpv4address(), instance.getGateway4());
            if(!ipv4){
                return ResponseUtil.badArgument("Ipv4地址和网关不一致");
            }
        }

        if(StringUtils.isNotEmpty(instance.getIpv6address()) && StringUtils.isNotEmpty(instance.getGateway6())){
            boolean ipv6 = this.isIPAddressv6MatchingGateway(instance.getIpv6address(), instance.getGateway6());

            if(!ipv6){
                return ResponseUtil.badArgument("Ipv6地址和网关不一致");
            }
        }

        boolean i = this.interfaceService.modify_ip(instance);


         boolean flag = i ? true : false;

         if(flag){
             unbound();
         }

        return flag ? ResponseUtil.ok() : ResponseUtil.badArgument("配置失败");
    }

    @ApiOperation("子接口")
    @PostMapping({"/modify/vlans"})
    public Object modifyVlans(@RequestBody Interface instance) {
        boolean flag=false;
        if (StringUtil.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument ("网络接口不能为空");
        }
        for (Vlans vlan : instance.getVlans()) {
            // 校验IPv4地址
            if (StringUtils.isNotEmpty(vlan.getIpv4address()) && !Ipv4Util.verifyCidr(vlan.getIpv4address())) {
                return ResponseUtil.badArgument("子接口IPv4地址格式错误，不符合CIDR格式");
            }
            // 校验IPv4网关
            if (StringUtils.isNotEmpty(vlan.getGateway4()) && !Ipv4Util.verifyIp(vlan.getGateway4())) {
                return ResponseUtil.badArgument("子接口IPv4网关格式错误");
            }
            // 校验IPv6地址
            if (StringUtils.isNotEmpty(vlan.getIpv6address()) && !Ipv6Util.verifyCidr(vlan.getIpv6address())) {
                return ResponseUtil.badArgument("子接口IPv6地址格式错误，不符合CIDR格式");
            }
            // 校验IPv6网关
            if (StringUtils.isNotEmpty(vlan.getGateway6()) && !Ipv6Util.verifyIpv6(vlan.getGateway6())) {
                return ResponseUtil.badArgument("子接口IPv6网关格式错误");
            }
            // 校验IPv4地址与网关是否匹配
            if (StringUtils.isNotEmpty(vlan.getIpv4address()) && StringUtils.isNotEmpty(vlan.getGateway4())) {
                boolean ipv4Match = isIPAddressMatchingGateway(vlan.getIpv4address(), vlan.getGateway4());
                if (!ipv4Match) {
                    return ResponseUtil.badArgument("子接口IPv4地址和网关不在同一网络");
                }
            }
            // 校验IPv6地址与网关是否匹配
            if (StringUtils.isNotEmpty(vlan.getIpv6address()) && StringUtils.isNotEmpty(vlan.getGateway6())) {
                boolean ipv6Match = isIPAddressv6MatchingGateway(vlan.getIpv6address(), vlan.getGateway6());
                if (!ipv6Match) {
                    return ResponseUtil.badArgument("子接口IPv6地址和网关不在同一网络");
                }
            }
            boolean i = this.interfaceService.modify_vlans(instance.getName(),vlan);
            flag = i ? true : false;
        }

        if(flag){
            unbound();
        }

        return flag ? ResponseUtil.ok() : ResponseUtil.badArgument("配置失败");
    }


    public boolean unbound() {
        Unbound unbound = this.unboundService.selectObjByOne(Collections.EMPTY_MAP);

        Set<String> list = new HashSet<>();
//        String path = Global.PYPATH + "getnetintf.py";
//        String result = pythonExecUtils.exec(path);
        String result = SNMPv2Request.getNetworkInterfaces();
        if(!"".equals(result)){
            LinkedHashMap<String, Object> map = JSONObject.parseObject(result, LinkedHashMap.class);
            for (String key : map.keySet()) {
                Interface inteface = JSONObject.parseObject(JSONObject.toJSONString(map.get(key)), Interface.class);
                if(inteface.getIsup().equals("up")){
                    if(Ipv6Util.verifyCidr(inteface.getIpv6address())){
                        String[] cidr = inteface.getIpv6address().split("/");
                        list.add(cidr[0]);
                    }
                }
            }
            unbound.setInterfaces(list);
            try {
                boolean flag = UnboundConfUtil.updateInterfaceFile(Global.unboundPath, unbound);
                if (!flag) {
                    return flag;
                }else{
                    boolean result1 = RestartUnboundUtils.restartUnboundService();
                    return result1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
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


    @DeleteMapping({"/delete"})
    public Object delete(String ids) {
        if (ids != null && !ids.equals("")) {
            String[] var2 = ids.split(",");
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String id = var2[var4];
                Map params = new HashMap();
                params.put("id", Long.parseLong(id));
                List<Interface> addressPools = this.interfaceService.selectObjByMap(params);
                if (addressPools.size() <= 0) {
                    return ResponseUtil.badArgument();
                }

                Interface addressPool = (Interface)addressPools.get(0);

                try {
                    int var9 = this.interfaceService.delete(Long.parseLong(id));
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
