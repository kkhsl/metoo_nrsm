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
        /*// 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);
        // 重启 Unbound 服务
        Session session = conn.openSession();
        // 检查 Unbound 服务状态
        session = conn.openSession();
        session.execCommand("systemctl status unbound");
        String statusOutput = consumeInputStream(session.getStdout());
        System.out.println("Unbound 状态:\n" + statusOutput);
        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        boolean isRunning = checkUnboundStatus(conn);
        // 关闭连接
        conn.close();
        if (isRunning) {
            return true;
        } else {
            return false;
        }*/
        return unboundService.status();
    }


    @GetMapping("/restart")
    public Boolean restart() throws Exception {
        /*// 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);
        // 重启 Unbound 服务
        Session session = conn.openSession();
        session.execCommand("systemctl restart unbound");
        Thread.sleep(1000);
        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        session = conn.openSession();


        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        boolean isRunning = checkUnboundStatus(conn);
        // 关闭连接
        conn.close();
        if (isRunning) {
            return true;
        } else {
            return false;
        }*/
        return unboundService.start();
    }

    @GetMapping("/stop")
    public Boolean stop() throws Exception {
        /*// 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);
        // 重启 Unbound 服务
        Session session = conn.openSession();
        session.execCommand("systemctl stop unbound");
        Thread.sleep(1000);
        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        session = conn.openSession();
        session.execCommand("systemctl status unbound");
        String statusOutput = consumeInputStream(session.getStdout());
        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        boolean isRunning = checkUnboundStatus(conn);
        // 关闭连接
        conn.close();
        if (isRunning) {
            return true;
        } else {
            return false;
        }*/
        return unboundService.stop();
    }


   /* private String consumeInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }


    private boolean checkUnboundStatus(Connection conn) throws Exception {
        Session session = conn.openSession();
        session.execCommand("systemctl status unbound");
        String statusOutput = consumeInputStream(session.getStdout());
        session.close(); // 关闭会话
        // 判断服务状态
        return statusOutput.contains("Active: active (running)");
    }
*/



    /*@DeleteMapping("/deleteAll")
    private Result delete(@RequestParam String id){
        boolean flag = this.unboundService.delete(Long.parseLong(id));
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @PostMapping("/savaAll")
    private Result unbound(@RequestBody UnboundDTO instance){
        boolean flag = this.unboundService.update(instance);
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }*/


}
