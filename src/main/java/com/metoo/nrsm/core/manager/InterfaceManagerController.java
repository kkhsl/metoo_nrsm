
package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import com.metoo.nrsm.core.utils.ip.IpV4Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.nspm.Interface;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
//        String path = "E:\\python\\project\\djangoProject\\app01\\getnetintf.py";
        String result = PythonExecUtils.exec(path);
        LinkedHashMap<String, Object> map = JSONObject.parseObject(result, LinkedHashMap.class);
        for (String key : map.keySet()) {
            System.out.println("key: " + key + " value: " + map.get(key));
            Interface inteface = JSONObject.parseObject(JSONObject.toJSONString(map.get(key)), Interface.class);
            inteface.setName(key);
            list.add(inteface);
        }
        return ResponseUtil.ok(list);
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
        int i = this.interfaceService.save(instance);
        return i >= 1 ? ResponseUtil.ok() : ResponseUtil.badArgument("配置失败");
    }

    @ApiOperation("编辑")
    @PostMapping({"/modify/ip"})
    public Object modifyIp(@RequestBody Interface instance) {
        if (StringUtil.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument("网络接口不能为空");
        }
        boolean i = this.interfaceService.modify_ip(instance);
        return i ? ResponseUtil.ok() : ResponseUtil.badArgument("配置失败");
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
