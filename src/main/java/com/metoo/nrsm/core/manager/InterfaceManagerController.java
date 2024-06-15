
package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.utils.ip.CIDRUtils;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6CIDRUtils;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Interface;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
        String path = "/opt/nrsm/py/getnetintf.py";
        String result = PythonExecUtils.exec(path);
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
        return i >= 1 ? ResponseUtil.ok() : ResponseUtil.badArgument("配置失败");
    }

    @ApiOperation("编辑")
    @PostMapping({"/modify/ip"})
    public Object modifyIp(@RequestBody Interface instance) {
        if (StringUtil.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument ("网络接口不能为空");
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

        boolean i = this.interfaceService.modify_ip(instance);
        return i ? ResponseUtil.ok() : ResponseUtil.badArgument("配置失败");
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
