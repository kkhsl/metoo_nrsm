package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IDomainService;
import com.metoo.nrsm.core.service.IPortService;
import com.metoo.nrsm.core.service.ISubnetService;
import com.metoo.nrsm.core.service.IVlanService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Domain;
import com.metoo.nrsm.entity.Port;
import com.metoo.nrsm.entity.Subnet;
import com.metoo.nrsm.entity.Vlan;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description Vlan管理
 *
 * @author HKK
 *
 * @create 2023/02/22
 *
 */
@RequestMapping("/admin/vlan")
@RestController
public class VlanManagerController {

    @Autowired
    private IVlanService vlanService;
    @Autowired
    private ISubnetService subnetService;
    @Autowired
    private IPortService portService;
    @Autowired
    private IDomainService domainService;

    @ApiOperation("Vlan列表")
    @GetMapping
    public Result list(@RequestParam(value = "domainId",required = false) Long domainId){
        Map params = new HashMap();
        Domain domain = this.domainService.selectObjById(domainId);
        if(domain != null){
            params.put("domainId", domain.getId());
        }
        params.put("hidden", false);
        params.put("orderBy", "number");
        params.put("orderType", "DESC");
        List<Vlan> vlans = this.vlanService.selectObjByMap(params);
        vlans.stream().forEach(e -> {
            if(e.getSubnetId() != null && !e.getSubnetId().equals("")) {
                Subnet subnet = this.subnetService.selectObjById(e.getSubnetId());
                e.setSubnetIp(subnet.getIp());
                e.setMaskBit(subnet.getMask());
            }
        });
        return ResponseUtil.ok(vlans);
    }

    @ApiOperation("Vlan添加")
    @GetMapping("/add")
    public Object add(){
        Map map = new HashMap();
        Map params = new HashMap();
        List<Domain> domains = this.domainService.selectObjByMap(params);
        map.put("domain", domains);
        List<Subnet> parentList = this.subnetService.selectSubnetByParentId(null);
        if(parentList.size() > 0){
            for (Subnet subnet : parentList) {
                this.genericSubnet(subnet);
            }
        }
        map.put("subnet", parentList);
        return ResponseUtil.ok(map);
    }

    public List<Subnet> genericSubnet(Subnet subnet){
        List<Subnet> subnets = this.subnetService.selectSubnetByParentId(subnet.getId());
        if(subnets.size() > 0){
            for(Subnet child : subnets){
                List<Subnet> subnetList = genericSubnet(child);
                if(subnetList.size() > 0){
                    child.setSubnetList(subnetList);
                }
            }
            subnet.setSubnetList(subnets);
        }
        return subnets;
    }

    @ApiOperation("Vlan更新,数据回显")
    @GetMapping("/update")
    public Object updadte(@RequestParam(value = "id") Long id){
        Map map = new HashMap();
        Vlan vlan = this.vlanService.selectObjById(id);
        if(vlan == null){
            return ResponseUtil.badArgument("Vlan不存在");
        }
        Domain domain = this.domainService.selectObjById(vlan.getDomainId());
        if(domain != null){
            vlan.setDomainName(domain.getName());
        }
        if(vlan.getSubnetId() != null){
            Subnet subnet = this.subnetService.selectObjById(vlan.getSubnetId());
            vlan.setSubnetIp(subnet.getIp());
            vlan.setMaskBit(subnet.getMask());
        }
        map.put("vlan", vlan);
        Map params = new HashMap();
        List<Domain> domains = this.domainService.selectObjByMap(params);
        map.put("domain", domains);
        List<Subnet> parentList = this.subnetService.selectSubnetByParentId(null);
        if(parentList.size() > 0){
            for (Subnet subnet : parentList) {
                this.genericSubnet(subnet);
            }
        }
        map.put("subnet", parentList);
        return ResponseUtil.ok(map);
    }

    @ApiOperation("创建/修改")
    @PostMapping
    public Object save(@RequestBody Vlan vlan){
//        if(vlan.getName() == null || vlan.getName().equals("")){
//            return ResponseUtil.badArgument("名称不能为空");
//        }else{
//            Map params = new HashMap();
//            params.put("vlanId", vlan.getId());
//            params.put("name", vlan.getName());
//            // 当前分组内不重名
//            User user = ShiroUserHolder.currentUser();
//            Group group = this.groupService.selectObjById(user.getGroupId());
//            if(group != null) {
//                Set<Long> ids = this.groupTools.genericGroupId(group.getId());
//                params.put("groupIds", ids);
//            }
//            List<Vlan> domains = this.vlanService.selectObjByMap(params);
//            if(domains.size() > 0){
//                return ResponseUtil.badArgument("名称重复");
//            }
//        }
//        if(vlan.getDomainId() != null && !vlan.getDomainId().equals("")){
//            Domain domain = this.domainService.selectObjById(vlan.getDomainId());
//            if(domain == null){
//                return ResponseUtil.badArgument("二层域不存在");
//            }
//        }
//        if(vlan.getSubnetId() != null){
//            Subnet subnet = this.subnetService.selectObjById(vlan.getSubnetId());
//            if(subnet == null){
//                return ResponseUtil.badArgument("网段不存在");
//            }
//        }
        if(ObjectUtils.allNotNull(vlan.getNumber())){
            return ResponseUtil.badArgument("Vlan号不允许编辑");
        }
        int result = this.vlanService.save(vlan);
        if(result >= 1){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @ApiOperation("删除")
    @DeleteMapping
    public Object delete(String ids){
        for (String id : ids.split(",")){
            Vlan vlan = this.vlanService.selectObjById(Long.parseLong(id));
            if(vlan == null){
                return ResponseUtil.badArgument();
            }
            int i = this.vlanService.delete(Long.parseLong(id));
            if(i <= 0){
                return ResponseUtil.error();
            }
        }
        return ResponseUtil.ok();
    }

    @GetMapping("/comb")
    public Object gatherVlan(){
        List<Port> ports = this.portService.selctVlanNumberBySplitFieldFunction();
        if(ports.size() > 0){
            try {
                Domain domain = null;
                Map params = new HashMap();
                params.clear();
                params.put("name", "默认二层域");
                List<Domain> domains = this.domainService.selectObjByMap(params);
                if(domains.size() > 0){
                    domain = domains.get(0);
                }
                for (Port port : ports) {
                    params.clear();
                    params.put("number", port.getVlanNumber());
                    List<Vlan> vlans = this.vlanService.selectObjByMap(params);
                    if(vlans.size() > 0){
                        Vlan vlan = vlans.get(0);
                        vlan.setNumber(port.getVlanNumber());
                        vlan.setHidden(false);
                        this.vlanService.update(vlan);
                    }else{
                        Vlan vlan = new Vlan();
                        vlan.setHidden(false);
                        vlan.setNumber(port.getVlanNumber());
                        if(domain != null){
                            vlan.setDomainId(domain.getId());
                        }
                        this.vlanService.save(vlan);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseUtil.error();
            }
        }
        return ResponseUtil.ok();
    }
}
