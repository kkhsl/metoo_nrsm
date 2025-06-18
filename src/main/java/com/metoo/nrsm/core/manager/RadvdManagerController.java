package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.RadvdDTO;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.service.IRadvdService;
import com.metoo.nrsm.core.service.impl.SubnetIpv6ServiceImpl;
import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.manager.SmartServiceManager;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;
import com.metoo.nrsm.core.utils.ip.Ipv6.IPv6SubnetCheck;
import com.metoo.nrsm.core.utils.ip.Ipv6.Ipv6CIDRUtils;
import com.metoo.nrsm.core.utils.net.Ipv6Utils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Interface;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.Radvd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/radvd")
public class RadvdManagerController {

    @Autowired
    private IRadvdService radvdService;
    @Autowired
    private IInterfaceService interfaceService;

    /**
     * 获取列表（分页）
     */
    @PostMapping("/list")
    public Object list(@RequestBody RadvdDTO dto) {
        // 开启分页
        Page<Radvd> page = this.radvdService.selectObjConditionQuery(dto);
        if(page.getResult().size() > 0){
            for (Radvd radvd : page.getResult()) {
                if(radvd.getInterfaceId() != null){
                    Interface obj = this.interfaceService.selectObjById(radvd.getInterfaceId());
                    if(obj != null){
                        radvd.setInterfaceName(obj.getName());
                        if(obj.getParentId() != null){
                            radvd.setInterfaceName(obj.getName()+"."+obj.getVlanNum());
                        }
                    }
                }
            }
        }
        Map map = new HashMap<>();
        List<Interface> interfaceList = this.interfaceService.selectObjByMap(Collections.emptyMap());
        map.put("interface", interfaceList);
        return ResponseUtil.ok(new PageInfo<Radvd>(page, map));
    }

    /**
     * 获取详情
     */
    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) {
        Radvd radvd = this.radvdService.selectObjById(id);
        if (radvd == null) {
            return ResponseUtil.badArgument("记录不存在");
        }
        return ResponseUtil.ok(radvd);
    }

    /**
     * 新增
     */
    @PostMapping("/save")
    public Object add(@RequestBody Radvd radvd) {
        // 基本参数验证
        if (radvd.getName() == null || radvd.getName().trim().isEmpty()) {
            return ResponseUtil.badArgument("名称不能为空");
        }else{
            Map params = new HashMap();
            params.put("name", radvd.getName());
            params.put("excludeId", radvd.getId());
            if (radvdService.selectObjByMap(params).size() >= 1) {
                return ResponseUtil.badArgument(radvd.getName() + "' 已经存在");
            }
        }

        if (radvd.getIpv6Prefix() == null || radvd.getIpv6Prefix().trim().isEmpty()) {
            return ResponseUtil.badArgument("请输入IPv6前缀");
        }else{
            boolean isCIDR = Ipv6CIDRUtils.verifyCIDR(radvd.getIpv6Prefix());
            if(!isCIDR){
                return ResponseUtil.badArgument("IPv6前缀格式错误");
            }
        }

        // 校验端口
        // 如果用户同时选择了一个接口的主接口和子接口，返回错误信息，让用户重新填写
        Interface instance = this.interfaceService.selectObjById(radvd.getInterfaceId());
        if(instance == null){
            return ResponseUtil.badArgument("请选择接口");
        }else{
            radvd.setInterfaceParentId(instance.getParentId());
            Result result = verifyInterface(radvd.getInterfaceId(), instance.getParentId(), radvd.getId());
            if(result != null){
               return result;
            }
            // 如果接口没问题，判断radvd ipv6前缀和接口ipv6地址是否在同一个网段
            String ipv6Address = instance.getIpv6Address();
            if(ipv6Address != null && StringUtil.isNotEmpty(ipv6Address)){
                String radvdPrefix = radvd.getIpv6Prefix();
                boolean checkSubnet = IPv6SubnetCheck.isInSameSubnet(radvdPrefix, instance.getIpv6Address());
                if(!checkSubnet){
                    return ResponseUtil.badArgument("IPv6前缀和接口IPv6网段不匹配");
                }
            }
        }
        if (this.radvdService.save(radvd)) {

            try {
                // 重启应用
                SmartServiceManager serviceManager = new SmartServiceManager();
                serviceManager.restartService("radvd");
            } catch (ServiceOperationException e) {
                e.printStackTrace();
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.updatedDataFailed();
    }


    public Result verifyInterface(Long interfaceId, Long interfaceParentId, Long id){
        Map params = new HashMap();
        // 如果主接口Id不为空，查询是否存在主接口
        if(interfaceParentId != null){
            params.clear();
            params.put("interfaceId", interfaceParentId);
            params.put("excludeId", id);
            List<Radvd> radvds = this.radvdService.selectObjByMap(params);
            // 如果主接口已存在，则不允许子接口选择
            if(radvds.size() > 0){
                return ResponseUtil.badArgument("不能同时选择主接口和它的子接口");
            }
        }else{
            // 如果选择的是主接口，查询是否存在子接口
            params.clear();
            params.put("interfaceParentId", interfaceId);
            params.put("excludeId", id);
            List<Radvd> radvds = this.radvdService.selectObjByMap(params);
            // 如果子接口已存在，则不允许主接口选择
            if(radvds.size() > 0){
                return ResponseUtil.badArgument("不能同时选择主接口和它的子接口");
            }
        }
        return null;
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id) {
        // 验证记录是否存在
        Radvd existing = this.radvdService.selectObjById(id);
        if (existing == null) {
            return ResponseUtil.badArgument("记录不存在");
        }

        if (this.radvdService.delete(id)) {
            try {
                // 重启应用
                SmartServiceManager serviceManager = new SmartServiceManager();
                serviceManager.restartService("radvd");
            } catch (ServiceOperationException e) {
                e.printStackTrace();
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.updatedDataFailed();
    }

}
