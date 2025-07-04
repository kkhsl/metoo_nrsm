package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.service.IUnboundService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Interface;
import com.metoo.nrsm.entity.Unbound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
        List<Interface> allInterfaces = interfaceService.selectAll();
        List<Interface> result = new ArrayList<>();

        for (String inputName : interfaceNames) {
            Interface output = new Interface();
            output.setName(inputName);
            output.setIpv4Address(null);
            output.setIpv6Address(null);

            try {
                if (inputName.contains(".")) {
                    String[] parts = inputName.split("\\.");
                    if (parts.length == 2) {
                        String parentName = parts[0];
                        int vlanNum = Integer.parseInt(parts[1]);

                        // 查找父接口时添加空集合保护
                        allInterfaces.stream()
                                .filter(p -> parentName.equals(p.getName()))
                                .findFirst()
                                .ifPresent(parent -> {
                                    // 处理可能为null的vlans列表
                                    List<Interface> vlans = parent.getVlans() != null ?
                                            parent.getVlans() :
                                            Collections.emptyList();

                                    vlans.stream()
                                            .filter(v -> v.getVlanNum() != null && v.getVlanNum() == vlanNum)
                                            .findFirst()
                                            .ifPresent(vlan -> {
                                                output.setIpv4Address(vlan.getIpv4Address());
                                                output.setIpv6Address(vlan.getIpv6Address());
                                            });
                                });
                    }
                } else {
                    // 主接口查询添加空指针保护
                    allInterfaces.stream()
                            .filter(m -> inputName.equals(m.getName()))
                            .findFirst()
                            .ifPresent(main -> {
                                output.setIpv4Address(main.getIpv4Address());
                                output.setIpv6Address(main.getIpv6Address());
                            });
                }
            } catch (NumberFormatException e) {
                // 记录日志：无效的VLAN编号格式
            } catch (Exception e) {
                // 记录系统异常日志
            }

            result.add(output);
        }

        return result;
    }

    @PostMapping("/savePort")
    public Result savePort(@RequestBody List<String> interfaceNames) throws Exception {
        List<Interface> instance = selectPort(interfaceNames);
        boolean flag = unboundService.savePort(instance);
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
