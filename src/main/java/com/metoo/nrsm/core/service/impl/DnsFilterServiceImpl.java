package com.metoo.nrsm.core.service.impl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.metoo.nrsm.core.mapper.DnsFilterMapper;
import com.metoo.nrsm.core.service.IDnsFilterService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.string.StringUtils;
import com.metoo.nrsm.core.utils.unbound.UnboundConfUtil;
import com.metoo.nrsm.entity.DnsFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DnsFilterServiceImpl implements IDnsFilterService {

    @Resource
    private DnsFilterMapper dnsFilterMapper;

    @Value("${ssh.hostname}")
    private String host;
    @Value("${ssh.port}")
    private int port;
    @Value("${ssh.username}")
    private String username;
    @Value("${ssh.password}")
    private String password;

    @Override
    @Transactional
    public boolean saveDnsFilter(DnsFilter dnsFilter) {
        try {
            // 参数校验
            if (StringUtils.isBlank(dnsFilter.getDomainName())) {
                throw new IllegalArgumentException("域名不能为空");
            }

            // 处理新增/更新逻辑
            boolean isUpdate = dnsFilter.getId() != null;

            // 检查域名冲突（新增时）
            if (!isUpdate && dnsFilterMapper.selectByDomainName(dnsFilter.getDomainName()) != null) {
                return false;
            }

            // 获取旧数据（更新时）
            String oldDomain = null;
            if (isUpdate) {
                DnsFilter oldRecord = dnsFilterMapper.selectById(Long.valueOf(dnsFilter.getId()));
                if (oldRecord == null) throw new RuntimeException("记录不存在");
                oldDomain = oldRecord.getDomainName();
            }

            // 设置时间戳
            if (isUpdate) {
                dnsFilter.setUpdateTime(new Date());
            } else {
                dnsFilter.setAddTime(new Date());
            }
            dnsFilter.setStatus(1);

            // 写入配置文件
            boolean writeSuccess = writeDnsFilter(dnsFilter, oldDomain);

            // 数据库操作
            int dbResult = isUpdate ?
                    dnsFilterMapper.update(dnsFilter) :
                    dnsFilterMapper.save(dnsFilter);

            return writeSuccess && dbResult > 0;
        } catch (Exception e) {
            //log.error("DNS过滤配置保存失败", e);
            throw new RuntimeException("操作失败", e);
        }
    }


/*    @Override
    @Transactional
    public boolean deleteDnsFilter(String ids) {
        try {
            // 1. 获取待删除记录
            DnsFilter existing = dnsFilterMapper.selectById(id);
            if (existing == null) {
                throw new IllegalArgumentException("配置不存在");
            }

            // 2. 从配置文件删除配置项
            boolean fileSuccess = removeFromConfigFile(existing.getDomainName());

            // 3. 从数据库删除记录
            int dbResult = dnsFilterMapper.delete(id);

            return fileSuccess && dbResult > 0;
        } catch (Exception e) {
            //log.error("删除DNS过滤配置失败 ID: {}", id, e);
            throw new RuntimeException("删除操作失败", e);
        }
    }*/

    @Override
    @Transactional
    public boolean deleteDnsFilter(String ids) {
        if (StringUtils.isBlank(ids)) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 存储所有需要删除的域名
        Set<String> domainsToRemove = new HashSet<>();
        List<Long> idList = new ArrayList<>();

        try {
            // 分割ID并逐个处理
            for (String idStr : ids.split(",")) {
                Long id = Long.parseLong(idStr.trim());
                DnsFilter existing = dnsFilterMapper.selectById(id);

                if (existing == null) {
                    throw new IllegalArgumentException("ID " + id + " 对应的配置不存在");
                }

                domainsToRemove.add(existing.getDomainName());
                idList.add(id);
            }

            // 批量数据库删除
            int deleteCount = 0;
            for (Long id : idList) {
                deleteCount += dnsFilterMapper.delete(id);
            }

            boolean fileSuccess = removeFromConfigFile(domainsToRemove);

            return deleteCount == idList.size() && fileSuccess;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID格式错误", e);
        } catch (Exception e) {
            //log.error("批量删除失败: {}", e.getMessage());
            throw new RuntimeException("删除操作失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean toggleDnsFilter(Long id, boolean enable) {
        try {
            // 获取目标记录
            DnsFilter dnsFilter = dnsFilterMapper.selectById(id);
            if (dnsFilter == null) {
                throw new IllegalArgumentException("DNS过滤规则不存在");
            }

            if (enable) {
                dnsFilter.setStatus(1);
            } else {
                dnsFilter.setStatus(0);
            }
            // 更新数据库状态
            dnsFilter.setUpdateTime(new Date());
            int dbResult = dnsFilterMapper.update(dnsFilter);

            // 同步配置文件
            boolean fileSuccess = updateConfigFileState(dnsFilter, enable);

            return dbResult > 0 && fileSuccess;
        } catch (Exception e) {
            //log.error("DNS过滤状态切换失败 ID: {}, 状态: {}", id, enable, e);
            throw new RuntimeException("操作失败", e);
        }
    }


    @Override
    public DnsFilter updateDNSFilter(Long id) {
        DnsFilter dnsFilter = dnsFilterMapper.selectById(id);
        return dnsFilter;
    }

    @Override
    public List<DnsFilter> selectAll(Map params) {
        return dnsFilterMapper.selectAll(params);
    }


    public boolean writeDnsFilter(DnsFilter newConfig, String oldDomain) throws Exception {
        boolean flag = UnboundConfUtil.writeConfigFile(Global.dnsFilterPath, newConfig, oldDomain);
        if (!flag) {
            throw new IOException("Failed to write config file");
        }
        return flag;
    }

    // 配置文件删除逻辑
    private boolean removeFromConfigFile(Set<String> domain) throws IOException {

        boolean flag = UnboundConfUtil.deleteConfigFile(Global.dnsFilterPath, domain);
        if (!flag) {
            throw new IOException("Failed to write config file");
        }

        return flag;
    }

    public boolean updateConfigFileState(DnsFilter config, boolean enable) throws Exception {
        boolean flag = UnboundConfUtil.writeConfigFile(Global.dnsFilterPath, config, enable);
        if (!flag) {
            throw new IOException("Failed to write config file");
        }
        return flag;
    }


    public boolean restart() throws Exception {
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);
        // 重启 Unbound 服务
        Session session = conn.openSession();
        session.execCommand("systemctl restart unbound");
        Thread.sleep(2500);
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
        }
    }

    public boolean stop1() throws Exception {
        // 创建连接
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
        }
    }

    public boolean status1() throws Exception {
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
        }
    }

    public boolean status2() throws Exception {
        // 检查 Unbound 服务状态
        ProcessBuilder statusBuilder = new ProcessBuilder("systemctl", "status", "unbound");
        statusBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process statusProcess = statusBuilder.start();

        // 读取输出
        String statusOutput = consumeInputStream2(statusProcess.getInputStream());
        statusProcess.waitFor(); // 等待状态检查完成

        // 打印状态信息
        System.out.println("Unbound 状态:\n" + statusOutput);

        // 检查服务是否正在运行
        boolean isRunning = checkUnboundStatus(statusOutput);

        return isRunning;
    }

    public boolean stop2() throws Exception {
        // 停止 Unbound 服务
        ProcessBuilder stopBuilder = new ProcessBuilder("systemctl", "stop", "unbound");
        stopBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process stopProcess = stopBuilder.start();
        stopProcess.waitFor(); // 等待停止命令完成

        // 检查 Unbound 服务状态
        ProcessBuilder statusBuilder = new ProcessBuilder("systemctl", "status", "unbound");
        statusBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process statusProcess = statusBuilder.start();

        String statusOutput = consumeInputStream2(statusProcess.getInputStream());
        statusProcess.waitFor(); // 等待状态检查完成

        // 检查服务是否正在运行
        boolean isRunning = checkUnboundStatus(statusOutput);

        return isRunning;
    }


    public boolean restart2() throws Exception {
        // 重启 Unbound 服务
        ProcessBuilder restartBuilder = new ProcessBuilder("systemctl", "restart", "unbound");
        restartBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process restartProcess = restartBuilder.start();
        restartProcess.waitFor(); // 等待重启完成

        // 检查 Unbound 服务状态
        ProcessBuilder statusBuilder = new ProcessBuilder("systemctl", "status", "unbound");
        statusBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process statusProcess = statusBuilder.start();

        String statusOutput = consumeInputStream2(statusProcess.getInputStream());
        statusProcess.waitFor(); // 等待状态检查完成

        // 检查服务是否正在运行
        boolean isRunning = checkUnboundStatus(statusOutput);

        return isRunning;
    }

    private String consumeInputStream2(InputStream inputStream) throws Exception {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

    private boolean checkUnboundStatus(String statusOutput) {
        return statusOutput.contains("active (running)");
    }

    public boolean start() {
        if ("TestAbstrack".equals(Global.env)) {
            try {
                return restart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return restart2();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean stop() {
        if ("TestAbstrack".equals(Global.env)) {
            try {
                return stop1();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return stop2();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean status() {
        if ("TestAbstrack".equals(Global.env)) {
            try {
                return status1();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return status2();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    private String consumeInputStream(InputStream inputStream) throws IOException {
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


}
