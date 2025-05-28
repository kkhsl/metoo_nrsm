package com.metoo.nrsm.core.service.impl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.metoo.nrsm.core.mapper.UnboundMapper;
import com.metoo.nrsm.core.service.INtpService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.unbound.UnboundConfUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@Service
public class NtpServiceImpl implements INtpService {

    @Resource
    private UnboundMapper unboundMapper;

    @Value("${ssh.hostname}")
    private String host;
    @Value("${ssh.port}")
    private int port;
    @Value("${ssh.username}")
    private String username;
    @Value("${ssh.password}")
    private String password;


    @Override
    public boolean open(Boolean instance) {
        if (instance){
            try {
                if ("test".equals(Global.env)) {
                    try {
                        return start1();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    try {
                        return start();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else {
            try {
                if ("test".equals(Global.env)) {
                    try {
                        return stop1();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    try {
                        return stop();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean openNtp(Boolean instance) {
        try {
            boolean flag = writeNtp(instance);
            if(flag){
                return true;
            }
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)  // 强制回滚所有异常
    public boolean saveTime(String instance) {
        try {
            boolean flag = writeTime(instance);
            if(flag){
                return true;
            }
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)  // 强制回滚所有异常
    public boolean saveNtp(List<String> instance) {
        try {
            boolean flag = writeNtpAdress(instance);
            if(flag){
                return true;
            }
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    public boolean writeTime(String instance) throws Exception {
        boolean flag = UnboundConfUtil.saveChronyConfigFile(Global.chronyPath,instance);
        if (!flag) {
            throw new IOException("Failed to save Pool address config file");
        }
        return flag;
    }
    public boolean writeNtp(Boolean instance) throws Exception {
        boolean flag = UnboundConfUtil.openNtpConfigFile(Global.chronyPath,instance);
        if (!flag) {
            throw new IOException("Failed to open Ntp config file");
        }
        return flag;
    }

    public boolean writeNtpAdress(List<String> instance) throws Exception {
        boolean flag = UnboundConfUtil.saveNtpConfigFile(Global.chronyPath,instance);
        if (!flag) {
            throw new IOException("Failed to save Ntp config file");
        }
        return flag;
    }



    @Override
    public Map<String, List<String>> select() {
        try {
            return UnboundConfUtil.selectChronyConfigFile(Global.chronyPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean restart() throws Exception {
        // 重启 chrony 服务
        ProcessBuilder restartBuilder = new ProcessBuilder("systemctl", "restart", "chrony");
        restartBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process restartProcess = restartBuilder.start();
        restartProcess.waitFor(); // 等待重启完成

        // 检查 chrony 服务状态
        ProcessBuilder statusBuilder = new ProcessBuilder("systemctl", "status", "chrony");
        statusBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process statusProcess = statusBuilder.start();

        String statusOutput = consumeInputStream(statusProcess.getInputStream());
        statusProcess.waitFor(); // 等待状态检查完成

        // 检查服务是否正在运行
        boolean isRunning = checkChronyStatus(statusOutput);

        return isRunning;
    }

    @Override
    public boolean env() throws Exception {
        if ("test".equals(Global.env)) {
            try {
                return restart1();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else{
            try {
                return restart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean restart1() throws Exception {
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);
        // 重启 chrony 服务
        Session session = conn.openSession();
        session.execCommand("systemctl restart chrony");
        Thread.sleep(2500);
        session.close(); // 关闭会话

        // 检查 chrony 服务状态
        session = conn.openSession();
        session.execCommand("systemctl status chrony");
        String statusOutput = consumeInputStream(session.getStdout());
        session.close(); // 关闭会话

        // 检查 chrony 服务状态
        boolean isRunning = checkChronyStatus(conn);
        // 关闭连接
        conn.close();
        if (isRunning) {
            return true;
        } else {
            return false;
        }
    }


    public boolean start() throws Exception {
        // 启动 chrony 服务
        ProcessBuilder restartBuilder = new ProcessBuilder("systemctl", "start", "chrony");
        restartBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process restartProcess = restartBuilder.start();
        restartProcess.waitFor(); // 等待重启完成

        // 检查 chrony 服务状态
        ProcessBuilder statusBuilder = new ProcessBuilder("systemctl", "status", "chrony");
        statusBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process statusProcess = statusBuilder.start();

        String statusOutput = consumeInputStream(statusProcess.getInputStream());
        statusProcess.waitFor(); // 等待状态检查完成

        // 检查服务是否正在运行
        boolean isRunning = checkChronyStatus(statusOutput);

        return isRunning;
    }
    public boolean start1() throws Exception {
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);
        // 重启 chrony 服务
        Session session = conn.openSession();
        session.execCommand("systemctl start chrony");
        Thread.sleep(2500);
        session.close(); // 关闭会话

        // 检查 chrony 服务状态
        session = conn.openSession();
        session.execCommand("systemctl status chrony");
        String statusOutput = consumeInputStream(session.getStdout());
        session.close(); // 关闭会话

        // 检查 chrony 服务状态
        boolean isRunning = checkChronyStatus(conn);
        // 关闭连接
        conn.close();
        if (isRunning) {
            return true;
        } else {
            return false;
        }
    }


    public boolean stop() throws Exception {
        // 停止 chrony 服务
        ProcessBuilder restartBuilder = new ProcessBuilder("systemctl", "stop", "chrony");
        restartBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process restartProcess = restartBuilder.start();
        restartProcess.waitFor(); // 等待重启完成

        // 检查 chrony 服务状态
        ProcessBuilder statusBuilder = new ProcessBuilder("systemctl", "stop", "chrony");
        statusBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process statusProcess = statusBuilder.start();

        String statusOutput = consumeInputStream(statusProcess.getInputStream());
        statusProcess.waitFor(); // 等待状态检查完成

        // 检查服务是否正在运行
        boolean isRunning = checkChronyStatus(statusOutput);

        return isRunning;
    }

    public boolean stop1() throws Exception {
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);
        // 重启 chrony 服务
        Session session = conn.openSession();
        session.execCommand("systemctl stop chrony");
        Thread.sleep(2500);
        session.close(); // 关闭会话

        // 检查 chrony 服务状态
        session = conn.openSession();
        session.execCommand("systemctl status chrony");
        String statusOutput = consumeInputStream(session.getStdout());
        session.close(); // 关闭会话

        // 检查 chrony 服务状态
        boolean isRunning = checkChronyStatus(conn);
        // 关闭连接
        conn.close();
        if (isRunning) {
            return true;
        } else {
            return false;
        }
    }

    public boolean status(){
        if ("test".equals(Global.env)) {
            try {
                return status2();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else{
            try {
                return status1();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean status1() throws Exception {
        // 检查 Unbound 服务状态
        ProcessBuilder statusBuilder = new ProcessBuilder("systemctl", "status", "chrony");
        statusBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process statusProcess = statusBuilder.start();

        // 读取输出
        String statusOutput = consumeInputStream(statusProcess.getInputStream());
        statusProcess.waitFor(); // 等待状态检查完成

        // 打印状态信息
        System.out.println("chrony 状态:\n" + statusOutput);

        // 检查服务是否正在运行
        boolean isRunning = checkChronyStatus(statusOutput);

        return isRunning;
    }

    public boolean status2() throws Exception {
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);
        // 重启 Unbound 服务
        Session session = conn.openSession();
        // 检查 Unbound 服务状态
        session = conn.openSession();
        session.execCommand("systemctl status chrony");
        String statusOutput = consumeInputStream(session.getStdout());
        System.out.println("chrony 状态:\n" + statusOutput);
        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        boolean isRunning = checkChronyStatus(conn);
        // 关闭连接
        conn.close();
        if (isRunning) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkChronyStatus(Connection conn) throws Exception {
        Session session = conn.openSession();
        session.execCommand("systemctl status chrony");
        String statusOutput = consumeInputStream(session.getStdout());
        session.close(); // 关闭会话
        // 判断服务状态
        return statusOutput.contains("Active: active (running)");
    }

    private boolean checkChronyStatus(String statusOutput) {
        return statusOutput.contains("active (running)");
    }

    private String consumeInputStream(InputStream inputStream) throws Exception {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }



}
