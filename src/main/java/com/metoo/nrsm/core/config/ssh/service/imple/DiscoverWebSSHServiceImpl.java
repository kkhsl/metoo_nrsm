package com.metoo.nrsm.core.config.ssh.service.imple;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.util.StringUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.config.ssh.excutor.ExecutorDto;
import com.metoo.nrsm.core.config.ssh.excutor.ExtendedRunnable;
import com.metoo.nrsm.core.config.ssh.ssh.constant.ConstantPool;
import com.metoo.nrsm.core.config.ssh.ssh.pojo.SSHConnectInfo;
import com.metoo.nrsm.core.config.ssh.ssh.pojo.WebSSHData;
import com.metoo.nrsm.core.config.ssh.ssh.service.impl.WebSSHServiceImpl;
import com.metoo.nrsm.core.service.ICredentialService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.entity.Credential;
import com.metoo.nrsm.entity.NetworkElement;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Executor;

@Service("commentWebSshService")
public class DiscoverWebSSHServiceImpl extends WebSSHServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(DiscoverWebSSHServiceImpl.class);
    private Logger logger = LoggerFactory.getLogger(DiscoverWebSSHServiceImpl.class);
    @Autowired
    @Qualifier("discoverSshExecutor")
    private Executor discoverSshExecutor;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private ICredentialService credentialService;
    public DiscoverWebSSHServiceImpl() {
    }

    public void recvHandle(String buffer, WebSocketSession session) {
        String userId = String.valueOf(session.getAttributes().get("user_uuid"));
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            final WebSSHData webSSHData = objectMapper.readValue(buffer, WebSSHData.class);
            if ("connect".equals(webSSHData.getOperate())) {
                final SSHConnectInfo sshConnectInfo = (SSHConnectInfo) ConstantPool.SSHMAP.get(userId);
                this.discoverSshExecutor.execute(new ExtendedRunnable(new ExecutorDto(userId, "", "", new Date())) {
                    protected void start() throws Exception {
                        try {
//                        DiscoverWebSSHServiceImpl.this.connectToSSH(sshConnectInfo, webSSHData, session);
//                        DiscoverWebSSHServiceImpl.this.connectToSSHTest(sshConnectInfo, session, "192.168.5.191",
//                                "metoo@domain", "Metoo@89745000");
                            DiscoverWebSSHServiceImpl.this.connectToSSH(sshConnectInfo, webSSHData, session);
                        } catch (IOException | JSchException var2) {
                            DiscoverWebSSHServiceImpl.this.logger.error("webssh连接异常");
                            DiscoverWebSSHServiceImpl.this.logger.error("异常信息:{}", var2.getMessage());
                            DiscoverWebSSHServiceImpl.this.close(session);
                            // 返回异常信息
                            Map map = new HashMap();
                            map.put("msg", var2.getLocalizedMessage()   );
                            sendMessage(session, JSONObject.toJSONBytes(map));
                        }
                    }
                });
            }else if ("command".equals(webSSHData.getOperate())) {
                String command = webSSHData.getCommand();
                SSHConnectInfo sshConnectInfo = (SSHConnectInfo) ConstantPool.SSHMAP.get(userId);
                if (sshConnectInfo != null) {
                    try {
                        this.transToSSH(sshConnectInfo.getChannel(), command);
                    } catch (IOException var9) {
                        this.logger.error("webssh连接异常");
                        this.logger.error("异常信息:{}", var9.getMessage());
                        this.close(session);
                    }
                }
            }
        } catch (IOException var10) {
            this.logger.error("Json转换异常");
            this.logger.error("异常信息:{}", var10.getMessage());
            return;
        }
    }


    public void connectToSSH(SSHConnectInfo sshConnectInfo, WebSSHData webSSHData, WebSocketSession webSocketSession) throws JSchException, IOException {
        String deviceUuid = webSSHData.getDeviceUuid();
        if (!StringUtils.isEmpty(deviceUuid)) {
            NetworkElement ne = this.networkElementService.selectObjByUuid(deviceUuid);
            if (ObjectUtils.isEmpty(ne)) {
                return;
            }
            if (ne.isPermitConnect()) {
                Credential credential = this.credentialService.getObjById(ne.getCredentialId());
                if (ObjectUtils.isEmpty(credential)) {
                    return;
                }
                Session session1 = sshConnectInfo.getjSch().getSession(credential.getLoginName(), ne.getIp(), ne.getPort());
                Session session = sshConnectInfo.getjSch().getSession(credential.getLoginName(), ne.getIp(), ne.getPort());

                if(!StringUtil.isEmpty(credential.getLoginPassword())){
                    session.setPassword(credential.getLoginPassword());
                }
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
//                config.put("PasswordAuthentication", "yes");
                session.setConfig(config);
                session.setDaemonThread(true);
                session.connect(30000);
                //shell 建立通道
                Channel channel = session.openChannel("shell");// sftp
                // 连接通道
                channel.connect(3000);

                sshConnectInfo.setChannel(channel);
                this.transToSSH(channel, "\r");

                InputStream inputStream = channel.getInputStream();

                try {
                    byte[] buffer = new byte[1024];
                    boolean var24 = false;

                    int i;
                    while ((i = inputStream.read(buffer)) != -1) {
                        this.sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
                    }
                } finally {
                    session.disconnect();
                    channel.disconnect();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        }
    }

    public void connectToSSHTest(SSHConnectInfo sshConnectInfo, WebSocketSession webSocketSession,
                 String ipv4, String loginName, String password) throws JSchException, IOException {

        Session session = sshConnectInfo.getjSch().getSession(loginName, ipv4, 22);

        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setDaemonThread(true);
        session.connect(30000);
        //shell 建立通道
        Channel channel = session.openChannel("shell");
        // 连接通道
        channel.connect(3000);

        sshConnectInfo.setChannel(channel);
        this.transToSSH(channel, "\r");

        InputStream inputStream = channel.getInputStream();

        try

        {
            byte[] buffer = new byte[1024];
            boolean var24 = false;

            int i;
            while ((i = inputStream.read(buffer)) != -1) {
                this.sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
            }
        } finally

        {
            session.disconnect();
            channel.disconnect();
            if (inputStream != null) {
                inputStream.close();
            }

        }
    }

    public void transToSSH(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }

    }
}