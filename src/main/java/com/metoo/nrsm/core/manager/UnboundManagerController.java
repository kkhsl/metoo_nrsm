package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.service.IUnboundService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Interface;
import com.metoo.nrsm.entity.Unbound;
import com.metoo.nrsm.entity.Vlans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@RequestMapping("/admin/unbound")
@RestController
public class UnboundManagerController {

    @Autowired
    private IUnboundService unboundService;

    @Autowired
    private IInterfaceService interfaceService;

    @Value("${ssh.hostname}")
    private String host;
    @Value("${ssh.port}")
    private int port = 22;
    @Value("${ssh.username}")
    private String username;
    @Value("${ssh.password}")
    private String password;



    @PostMapping("/save")
    private Result add(@RequestBody UnboundDTO instance) {
        boolean flag = this.unboundService.add(instance);
        if (flag) {
            try {
                if (restart()) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("启动失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseUtil.error("保存失败");
    }


    @DeleteMapping("/delete")
    private Result unbound(@RequestParam String id) {
        boolean flag = this.unboundService.delete(Long.parseLong(id));
        if (flag) {
            try {
                if (restart()) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("启动失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseUtil.error("重复删除");
    }

    @GetMapping("/select")
    private Result unbound() {
        Unbound unbound = this.unboundService.selectObjByOne(Collections.emptyMap());
        return ResponseUtil.ok(unbound);
    }


    @PostMapping("/saveDNS")
    private Result DNS(@RequestBody UnboundDTO instance) {
        boolean flag = this.unboundService.addDNS(instance);
        if (flag) {
            try {
                if (restart()) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("启动失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseUtil.error();
    }

    @DeleteMapping("/resetDNS")
    private Result resetDNS(@RequestParam String id) {
        boolean flag = this.unboundService.deleteDNS(Long.parseLong(id));
        if (flag) {
            try {
                if (restart()) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("启动失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseUtil.error("重复删除");
    }

    @PostMapping("/openAddress")
    private Result openAddress(@RequestBody UnboundDTO instance) {
        boolean flag = this.unboundService.open(instance);
        if (flag) {
            try {
                if (restart()) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("启动失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseUtil.error();
    }

    @GetMapping("/status")
    public Boolean status() throws Exception {
        return unboundService.status();
    }


    @GetMapping("/restart")
    public Boolean restart() throws Exception {
        return unboundService.start();
    }


    public List<Interface> selectPort(List<String> interfaceNames) {
        List<Interface> allInterfaces = interfaceService.select();
        List<Interface> result = new ArrayList<>();

        for (String name : interfaceNames) {
            // 判断是否为 VLAN 子接口（格式：父接口名.VLAN_ID）
            if (name.contains(".")) {
                String[] parts = name.split("\\.");
                if (parts.length != 2) {
                    continue; // 格式错误，跳过
                }

                String parentName = parts[0];
                String vlanId = parts[1];

                // 查找父接口
                Optional<Interface> parentInterfaceOpt = allInterfaces.stream()
                        .filter(intf -> parentName.equals(intf.getName()))
                        .findFirst();

                if (parentInterfaceOpt.isPresent()) {
                    Interface parent = parentInterfaceOpt.get();
                    // 查找匹配的 VLAN
                    //DOTO
                    Optional<Interface> matchedVlan = parent.getVlans().stream()
                            .filter(vlan -> vlanId.equals(vlan.getId()))
                            .findFirst();

                    if (matchedVlan.isPresent()) {
                        // 构建子接口信息
                        Interface subInterface = new Interface();
                        subInterface.setName(name); // 名称如 enp2s0f1.200
                        subInterface.setIpv4Address(matchedVlan.get().getIpv4Address());
                        subInterface.setIpv6Address(matchedVlan.get().getIpv6Address());
                        result.add(subInterface);
                    }
                }
            } else {
                // 匹配物理接口
                allInterfaces.stream()
                        .filter(intf -> name.equals(intf.getName()))
                        .findFirst()
                        .ifPresent(intf -> {
                            // 复制所需字段
                            Interface matched = new Interface();
                            matched.setName(intf.getName());
                            matched.setIpv4Address(intf.getIpv4Address());
                            matched.setIpv6Address(intf.getIpv6Address());
                            result.add(matched);
                        });
            }
        }
        return result;
    }

    @PostMapping("/savePort")
    public Result savePort(@RequestBody List<String> interfaceNames) throws Exception {
        List<Interface> instance = selectPort(interfaceNames);
        boolean flag = unboundService.savePort(instance);
        if (flag){
            try {
                if (restart()) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("启动失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseUtil.error();
    }

    @GetMapping("/selectPort")
    public Result selectPort() throws Exception {
        List<String> strings = unboundService.selectPort();
        return ResponseUtil.ok(strings);
    }

    @GetMapping("/stop")
    public Boolean stop() throws Exception {
        return unboundService.stop();
    }






}
