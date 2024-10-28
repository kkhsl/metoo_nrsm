package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IFluxConfigService;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.core.utils.py.ssh.Ssh2Demo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.wsapi.utils.Md5Crypt;
import com.metoo.nrsm.entity.FluxConfig;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 11:36
 */
@RestController
@RequestMapping("/admin/flux/config")
public class FluxConfigManagerController {

    @Autowired
    private IFluxConfigService fluxConfigService;

    @GetMapping
    public Result all(){
        List<FluxConfig> fluxConfigList = this.fluxConfigService.selectObjByMap(null);
        if(fluxConfigList.size() > 0){
            for (FluxConfig fluxConfig : fluxConfigList) {
                List<List<String>> v4_oids = JSONObject.parseObject(fluxConfig.getIpv4Oid(), List.class);
                List<List<String>> v6_oids = JSONObject.parseObject(fluxConfig.getIpv6Oid(), List.class);
                fluxConfig.setIpv4Oids(v4_oids);
                fluxConfig.setIpv6Oids(v6_oids);
            }
        }
        return ResponseUtil.ok(fluxConfigList);
    }

    @PostMapping
    public Object save(@RequestBody FluxConfig fluxConfig){
        if(StringUtils.isNotEmpty(fluxConfig.getIpv4())){
            boolean flag = Ipv4Util.verifyIp(fluxConfig.getIpv4());
            if(!flag){
                return ResponseUtil.badArgument("ipv4格式错误");
            }
        }
        if(!StringUtils.isNotEmpty(fluxConfig.getIpv6())){
            boolean flag = Ipv6Util.verifyIpv6(fluxConfig.getIpv6());
            if(flag){
                return ResponseUtil.badArgument("ipv6格式错误");
            }
        }

        if(fluxConfig.getIpv4Oids() != null){
            String ipv4oid = JSONObject.toJSONString(fluxConfig.getIpv4Oids());
            fluxConfig.setIpv4Oid(ipv4oid);
        }

        if(fluxConfig.getIpv6Oids() != null){
            String ipv6oid = JSONObject.toJSONString(fluxConfig.getIpv6Oids());
            fluxConfig.setIpv6Oid(ipv6oid);
        }

        if(fluxConfig.getId() != null || !fluxConfig.getId().equals("")){
            FluxConfig obj = this.fluxConfigService.selectObjById(fluxConfig.getId());
            fluxConfig.getIpv4Oids().clear();
            fluxConfig.getIpv6Oids().clear();
            boolean flag = Md5Crypt.getDiffrent(obj, fluxConfig);
            if(!flag){
                fluxConfig.setUpdate(1);
            }
        }
        boolean flag = this.fluxConfigService.save(fluxConfig);
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @DeleteMapping
    public Object delete(String ids){
        if(ids != null && !ids.equals("")){
            for (String id : ids.split(",")){
                Map params = new HashMap();
                params.put("id", Long.parseLong(id));
                List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(params);
                if(fluxConfigs.size() > 0){
                    FluxConfig fluxConfig = fluxConfigs.get(0);
                    try {
                        boolean i = this.fluxConfigService.delete(Long.parseLong(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return ResponseUtil.badArgument(fluxConfig.getName() + "删除失败");
                    }
                }else{
                    return ResponseUtil.badArgument();
                }
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }



    @GetMapping("/gather")
    public void gather(){
        List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(null);
        if(fluxConfigs.size() > 0){
            BigDecimal ipv4Sum = new BigDecimal(0);
            BigDecimal ipv6Sum = new BigDecimal(0);

            BigDecimal ipv6Rate = new BigDecimal(0);

            // 获取全部ipv4流量
            List<Map<String, String>> v4_list = new ArrayList<>();
            List<Map<String, String>> v6_list = new ArrayList<>();
            for (FluxConfig fluxConfig : fluxConfigs) {
                // 获取ipv4流量
                // 1. 遍历oid
                List<List<String>> v4_oids =JSONObject.parseObject(fluxConfig.getIpv4Oid(), List.class);
                if(v4_oids.size() > 0){
                    for (List<String> oid : v4_oids) {
                        if(oid.size() > 0){
                            String in = String.valueOf(oid.get(0));
                            String out = String.valueOf(oid.get(1));
                            String result = exec_v4(fluxConfig.getIpv4(), in, out);
                            if(StringUtils.isNotEmpty(result)){
                                Map map = JSONObject.parseObject(result, Map.class);
                                v4_list.add(map);
                            }
                        }
                    }
                }

                List<List<String>> v6_oids =JSONObject.parseObject(fluxConfig.getIpv4Oid(), List.class);
                if(v6_oids.size() > 0){
                    for (List<String> oid : v6_oids) {
                        if(oid.size() > 0){
                            String in = String.valueOf(oid.get(0));
                            String out = String.valueOf(oid.get(1));
                            String result = exec_v6(fluxConfig.getIpv4(), in, out);
                            if(StringUtils.isNotEmpty(result)){
                                Map map = JSONObject.parseObject(result, Map.class);
                                v6_list.add(map);
                            }
                        }
                    }
                }
            }

            if(v4_list.size() > 0){
                BigDecimal in = v4_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal out = v4_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
                ipv4Sum = in.add(out);
                System.out.println(ipv4Sum);
            }

            if(v6_list.size() > 0){
                BigDecimal in = v6_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal out = v6_list.stream().map(x ->
                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
                ipv6Sum = in.add(out);
                System.out.println(ipv6Sum);
            }
            // ipv6流量占比=ipv6流量/（ipv4流量+ipv6流量）
            ipv6Rate = ipv6Sum.divide(ipv4Sum.add(ipv6Sum));

        }
    }

    public String exec_v4(String ip, String in, String out) {
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {ip, "v2c",
                "public@123", in, out};
        String result = Ssh2Demo.exec(path, params);
        if(StringUtil.isNotEmpty(result)){
            return result;
        }
        return null;
    }

    public String exec_v6(String ip, String in, String out) {
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {ip, "v2c",
                "public@123", in, out};
        String result = Ssh2Demo.exec(path, params);
        if(StringUtil.isNotEmpty(result)){
            return result;
        }
        return null;
    }



    @Test
    public void test(){
        List<List<String>> list = new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        stringList.add("a");
        stringList.add("");
        stringList.add("c");
        list.add(stringList);

        String s = JSONObject.toJSONString(list);
        System.out.println(s);
        List<List<String>> list1 =JSONObject.parseObject(s, List.class);
        System.out.println(list1);
    }
}
