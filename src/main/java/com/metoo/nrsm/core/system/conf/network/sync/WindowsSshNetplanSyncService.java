package com.metoo.nrsm.core.system.conf.network.sync;

import com.jcraft.jsch.*;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.entity.Interface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WindowsSshNetplanSyncService {

    @Autowired
    private NetplanParserService parserService;
    @Autowired
    private IInterfaceService interfaceService;

//    private static final String NETPLAN_FILE = "/etc/netplan/00-installer-config.yaml";

    @Value("${ssh.hostname}")
    private String host;
    @Value("${ssh.port}")
    private int port;
    @Value("${ssh.username}")
    private String username;
    @Value("${ssh.password}")
    private String password;
    @Value("${network.netplan.file}")
    private String netplan_file;


    @Transactional
    public void syncInterfaces() {
        Session session = null;
        ChannelExec channel = null;

        try {
            // 建立SSH连接
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 执行命令获取配置
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("cat " + netplan_file);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            channel.setOutputStream(outputStream);
            channel.connect();

            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            // 1. 解析配置
            InputStream configStream = new ByteArrayInputStream(outputStream.toByteArray());
            List<Interface> interfaces = parserService.parseNetplanConfig(configStream);

            // 2. 统一保存到数据库
            saveAllInterfaces(interfaces);

        } catch (Exception e) {
            throw new RuntimeException("远程同步网络接口配置失败", e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    /**
     * 不用考虑更新，不用考虑更新主接口其他数据
     *
     * @param interfaces
     */

    private void saveAllInterfaces(List<Interface> interfaces) {
        // 先保存主接口(无parentId的)
        Map params = new HashMap();
        for (Interface anInterface : interfaces) {
            params.clear();
            params.put("name", anInterface.getName());
            params.put("parentId", true);
            List<Interface> intf = this.interfaceService.selectObjByMap(params);
            if (intf.size() <= 0) {
                interfaceService.save(anInterface);
            }
        }
    }
}