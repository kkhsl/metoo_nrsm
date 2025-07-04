package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ISubnetIpv6Service;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.SubnetIpv6;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-24 15:23
 */
@RequestMapping("/admin/subnet/ipv6")
@RestController
public class SubnetIpv6ManagerController {

    @Autowired
    private ISubnetIpv6Service subnetIpv6Service;

    @GetMapping("/comb")
    public Object comb() {
        return this.subnetIpv6Service.getSubnet();
    }

    @GetMapping("list")
    public Result list() {
        // 获取所有子网一级
        List<SubnetIpv6> parentList = this.subnetIpv6Service.selectSubnetByParentId(null);
        if (parentList.size() > 0) {
            for (SubnetIpv6 subnet : parentList) {
                this.genericSubnet(subnet);
            }
            return ResponseUtil.ok(parentList);
        }
        return ResponseUtil.ok();
    }

    public List<SubnetIpv6> genericSubnet(SubnetIpv6 subnetIpv6) {
        List<SubnetIpv6> subnets = this.subnetIpv6Service.selectSubnetByParentId(subnetIpv6.getId());
        if (subnets.size() > 0) {
            for (SubnetIpv6 child : subnets) {
                List<SubnetIpv6> subnetList = genericSubnet(child);
                if (subnetList.size() > 0) {
                    child.setSubnetList(subnetList);
                }
            }
            subnetIpv6.setSubnetList(subnets);
        }
        return subnets;
    }

    @ApiOperation("根据网段Ip查询直接从属子网")
    @GetMapping(value = {"", "/{id}"})
    public Object getSubnet(@PathVariable(value = "id", required = false) Long id) {
        SubnetIpv6 subnetIpv6 = this.subnetIpv6Service.selectObjById(id);
        if (subnetIpv6 != null) {
            // 当前网段
            Map map = new HashMap();
            map.put("subnetIpv6", subnetIpv6);
            return ResponseUtil.ok(map);
        }
        return ResponseUtil.ok();
    }


    @PutMapping
    public Result update(@RequestBody SubnetIpv6 instance) {
        return this.subnetIpv6Service.update(instance);
    }
}
