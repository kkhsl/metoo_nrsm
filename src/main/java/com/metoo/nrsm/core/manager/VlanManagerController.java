package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.*;
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
    @Autowired
    private ISubnetIpv6Service subnetIpv6Service;

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
                if(subnet != null){
                    e.setSubnetIp(subnet.getIp());
                    e.setMaskBit(subnet.getMask());
                }
            }
            if(e.getSubnetIdIpv6() != null && !e.getSubnetIdIpv6().equals("")) {
                SubnetIpv6 subnetIpv6 = this.subnetIpv6Service.selectObjById(e.getSubnetIdIpv6());
                if(subnetIpv6 != null){
                    e.setMaskBitIpv6(subnetIpv6.getMask());
                    e.setSubnetIpv6(subnetIpv6.getIp());
                }
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
        List<SubnetIpv6> parentListIpv6 = this.subnetIpv6Service.selectSubnetByParentId(null);
        if (parentListIpv6.size() > 0) {
            for (SubnetIpv6 subnet : parentListIpv6) {
                this.genericSubnetIpv6(subnet);
            }
        }
        map.put("subnetIpv6", parentListIpv6);
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

    public List<SubnetIpv6> genericSubnetIpv6(SubnetIpv6 subnetIpv6) {
        List<SubnetIpv6> subnets = this.subnetIpv6Service.selectSubnetByParentId(subnetIpv6.getId());
        if (subnets.size() > 0) {
            for (SubnetIpv6 child : subnets) {
                List<SubnetIpv6> subnetList = genericSubnetIpv6(child);
                if (subnetList.size() > 0) {
                    child.setSubnetList(subnetList);
                }
            }
            subnetIpv6.setSubnetList(subnets);
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

        if(vlan.getSubnetIdIpv6() != null){
            SubnetIpv6 subnetIpv6 = this.subnetIpv6Service.selectObjById(vlan.getSubnetIdIpv6());
            vlan.setMaskBitIpv6(subnetIpv6.getMask());
            vlan.setSubnetIpv6(subnetIpv6.getIp());
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
        List<SubnetIpv6> parentListIpv6 = this.subnetIpv6Service.selectSubnetByParentId(null);
        if (parentListIpv6.size() > 0) {
            for (SubnetIpv6 subnet : parentListIpv6) {
                this.genericSubnetIpv6(subnet);
            }
        }
        map.put("subnetIpv6", parentListIpv6);
        return ResponseUtil.ok(map);
    }

    @ApiOperation("创建/修改")
    @PostMapping
    public Object save(@RequestBody Vlan vlan){
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
