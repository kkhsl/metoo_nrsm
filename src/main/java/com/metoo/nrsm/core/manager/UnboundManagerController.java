package com.metoo.nrsm.core.manager;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.core.service.IUnboundService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unbound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;


@RequestMapping("/admin/unbound")
@RestController
public class UnboundManagerController {

    @Autowired
    private IUnboundService unboundService;
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

    @GetMapping("/stop")
    public Boolean stop() throws Exception {
        return unboundService.stop();
    }






}
