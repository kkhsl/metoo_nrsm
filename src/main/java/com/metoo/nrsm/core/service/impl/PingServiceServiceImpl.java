package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.PingMapper;
import com.metoo.nrsm.core.service.IDNSService;
import com.metoo.nrsm.core.service.IDnsRunStatusService;
import com.metoo.nrsm.core.service.IPingService;
import com.metoo.nrsm.core.service.IPingStartServiceParam;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.py.ssh.SshExec;
import com.metoo.nrsm.entity.DnsRunStatus;
import com.metoo.nrsm.entity.Ping;
import com.metoo.nrsm.entity.PingStartParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 17:38
 */
@Service
@Transactional
public class PingServiceServiceImpl implements IPingService {

    @Autowired
    private PingMapper pingMappe;
    @Autowired
    private IPingStartServiceParam pingStartServiceParam;
    @Autowired
    private IDnsRunStatusService dnsRunStatusService;

    @Override
    public Ping selectOneObj() {
        return this.pingMappe.selectOneObj();
    }

    @Override
    public void killDns() {
//        String path = Global.PYPATH + "processkill.py";
//        String[] params = {"dnsredis"};
//        String result = PythonExecUtils.exec(path, params);
        String command = "/opt/autostart/dnsredis.sh stop";
        SshExec.exec(command);
    }

    @Override
    public void startDns(String param) {
//        String path = Global.PYPATH + "dnsredis.py";
//        String[] params = {"dnsredis", param,"&"};
//        String result = PythonExecUtils.exec(path, params, "nohup");

        String command = "/opt/autostart/dnsredis.sh start " + param;
        SshExec.exec(command);
    }

    @Override
    public void exec(){

        DnsRunStatus dnsRunStatus = this.dnsRunStatusService.selectOneObj();

        Ping ping = this.selectOneObj();
        if(ping != null){
            PingStartParam pingStartParam = this.pingStartServiceParam.selectOneObj();
            boolean checkdns = this.dnsRunStatusService.checkdns();
            if(!checkdns){
                if(ping.getV6isok().equals("0")){
                    this.killDns();
                    this.startDns("1");
                    pingStartParam.setParam("1");
                    this.pingStartServiceParam.update(pingStartParam);
                    dnsRunStatus.setStatus(true);
                    this.dnsRunStatusService.update(dnsRunStatus);

                }
                if(ping.getV6isok().equals("1")){
                    this.killDns();
                    this.startDns("0");
                    pingStartParam.setParam("0");
                    this.pingStartServiceParam.update(pingStartParam);
                    dnsRunStatus.setStatus(true);
                    this.dnsRunStatusService.update(dnsRunStatus);

                }
            }
            if(ping.getV6isok().equals("0")){
                if("0".equals(pingStartParam.getParam())){
                    this.killDns();
                    this.startDns("1");
                    pingStartParam.setParam("1");
                    this.pingStartServiceParam.update(pingStartParam);
                    dnsRunStatus.setStatus(true);
                    this.dnsRunStatusService.update(dnsRunStatus);
                }
            }
            if(ping.getV6isok().equals("1")){
                if("1".equals(pingStartParam.getParam())){
                    this.killDns();
                    this.startDns("0");
                    pingStartParam.setParam("0");
                    this.pingStartServiceParam.update(pingStartParam);
                    dnsRunStatus.setStatus(true);
                    this.dnsRunStatusService.update(dnsRunStatus);
                }
            }
        }
    }
}
