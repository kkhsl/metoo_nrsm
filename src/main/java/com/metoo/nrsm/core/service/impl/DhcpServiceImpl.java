package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.dto.DhcpDto;
import com.metoo.nrsm.core.mapper.DhcpMapper;
import com.metoo.nrsm.core.service.IDhcpHistoryService;
import com.metoo.nrsm.core.service.IDhcpService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.command.DhcpdConfigReader;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.dhcp.DhcpUtils;
import com.metoo.nrsm.entity.Dhcp;
import com.metoo.nrsm.entity.Internet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.*;
import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-16 11:14
 */
@Service
@Transactional
public class DhcpServiceImpl implements IDhcpService {

    @Autowired
    private DhcpMapper dhcpMapper;
    @Autowired
    private IDhcpHistoryService dhcphistoryService;
    @Autowired
    private PythonExecUtils pythonExecUtils;

    @Override
    public Dhcp selectObjById(Long id) {
        return this.dhcpMapper.selectObjById(id);
    }

    @Override
    public Dhcp selectObjByLease(String lease) {
        return this.dhcpMapper.selectObjByLease(lease);
    }

    @Override
    public Page<Dhcp> selectConditionQuery(DhcpDto dto) {
        if (dto == null) {
            dto = new DhcpDto();
        }
        Page<Dhcp> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.dhcpMapper.selectConditionQuery(dto);
        return page;
    }

    @Override
    public List<Dhcp> selectObjByMap(Map params) {
        return this.dhcpMapper.selectObjByMap(params);
    }

    @Override
    public boolean save(Dhcp instance) {
        if (instance.getId() == null) {
            try {
                this.dhcpMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                this.dhcpMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean update(Dhcp instance) {
        try {
            this.dhcpMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            this.dhcpMapper.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean truncateTable() {
        try {
            this.dhcpMapper.truncateTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTable() {
        try {
            this.dhcpMapper.deleteTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getdhcp() {
        String path = Global.PYPATH + "getdhcp.py";
        String result = pythonExecUtils.exec(path);
        return result;
    }

    @Override
    public String modifydhcp(Internet instance) {
        String path = Global.PYPATH + "modifydhcp.py";
        String[] params = {instance.getV4status(), instance.getV4int(),
                instance.getV6status(), instance.getV6int()};
        String result = pythonExecUtils.exec(path, params);
        return result;
    }

    @Override
    public String checkdhcpd(String type) {
        String path = Global.PYPATH + "checkdhcpd.py";
        String[] params = {type};
        String result = pythonExecUtils.exec(path, params);
        return result;
    }

    @Override
    public String dhcpdop(String action, String type) {
        String path = Global.PYPATH + "dhcpdop.py";
        String[] params = {action, type};
        String result = pythonExecUtils.exec(path, params);
        return result;
    }

    @Override
    public void gather(Date time) {
        this.deleteTable();
        Map<String, String> data = null;
        List<Map<String, String>> dataList = new ArrayList();
        DhcpdConfigReader reader = new DhcpdConfigReader();
        try {
            // 示例: 开启 dev 模式读取
            List<String> lines = reader.readDhcpdConfig(Global.env, Global.host, Global.port, Global.username, Global.password, Global.dhcp);
            for (String line : lines) {
                if (StringUtil.isNotEmpty(line)) {
                    line = line.trim();
                    String key = DhcpUtils.getKey(line);
                    if (StringUtil.isNotEmpty(key)) {
                        if (key.equals("lease")) {
                            if (data != null) {
                                dataList.add(data);
                            }else{
                                data = new HashMap();
                            }
                        }
                        if(data != null){
                            DhcpUtils.parseValue(key, line, data);
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (data != null && StringUtils.isNotBlank(data.get("lease"))) {
            dataList.add(data);
        }

        if (dataList.size() > 0) {
            for (Map<String, String> map : dataList) {
                Map<String, String> modifiedMap = new HashMap();
                Set<Map.Entry<String, String>> set = map.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    String key = entry.getKey();
                    if (key.contains(" ")) {
                        key = key.replaceAll(" ", "_");
                    }
                    if (key.contains("-")) {
                        key = key.replaceAll("-", "_");
                    }
                    modifiedMap.put(key, entry.getValue());
                }
                Dhcp dhcp = new Dhcp();
                dhcp.setAddTime(time);
                BeanMap beanMap = BeanMap.create(dhcp);
                beanMap.putAll(modifiedMap);
                this.save(dhcp);
            }
        }
        this.dhcphistoryService.batchInsert();
    }

    public void gather2(Date time) {
        try {

            this.deleteTable();

            InputStream inputStream = null;

            if (Global.env.equals("prod")) {
//                File file = new File("/var/lib/dhcp/dhcpd.leases");
                File file = new File(Global.dhcp);
                inputStream = new FileInputStream(file);
            } else if ("dev".equals(Global.env)) {
                inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("./dhcpd/dhcpd.leases");
            }
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    Map<String, String> data = null;
                    List<Map<String, String>> dataList = new ArrayList();
                    while ((line = reader.readLine()) != null) {
                        if (StringUtil.isNotEmpty(line)) {
                            line = line.trim();
                            String key = DhcpUtils.getKey(line);
                            if (StringUtil.isNotEmpty(key)) {
                                if (key.equals("lease")) {
                                    if (data != null) {
                                        dataList.add(data);
                                    }
                                    data = new HashMap();
                                }
                                DhcpUtils.parseValue(key, line, data);
                            }

                        }

                    }
                    // 最后一个
                    if (data != null && StringUtils.isNotBlank(data.get("lease"))) {
                        dataList.add(data);
                    }

                    if (dataList.size() > 0) {
                        for (Map<String, String> map : dataList) {
                            Map<String, String> modifiedMap = new HashMap();
                            Set<Map.Entry<String, String>> set = map.entrySet();
                            for (Map.Entry<String, String> entry : set) {
                                String key = entry.getKey();
                                if (key.contains(" ")) {
                                    key = key.replaceAll(" ", "_");
                                }
                                if (key.contains("-")) {
                                    key = key.replaceAll("-", "_");
                                }
                                modifiedMap.put(key, entry.getValue());
                            }
                            Dhcp dhcp = new Dhcp();
                            dhcp.setAddTime(time);
                            BeanMap beanMap = BeanMap.create(dhcp);
                            beanMap.putAll(modifiedMap);
                            this.save(dhcp);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            this.dhcphistoryService.batchInsert();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

        public static void main(String[] args) {
            String host = "192.168.6.101"; // 远程主机 IP
            int port = 22; // SSH 端口
            String user = "root"; // 用户名
            String password = "Metoo89745000!"; // 密码
            String filePath = "/etc/dhcp/dhcpd.conf"; // 配置文件路径

            try {
                // 初始化 JSch
                JSch jsch = new JSch();
                Session session = jsch.getSession(user, host, port);

                // 设置密码
                session.setPassword(password);

                // 忽略主机验证
                session.setConfig("StrictHostKeyChecking", "no");

                // 建立连接
                System.out.println("Connecting to " + host + "...");
                session.connect();
                System.out.println("Connected to " + host);

                // 执行命令以读取文件内容
                String command = "cat " + filePath;
                ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
                channelExec.setCommand(command);

                // 获取命令的输入流
                InputStream in = channelExec.getInputStream();

                // 打开通道
                channelExec.connect();

                // 读取文件内容
                try (Scanner scanner = new Scanner(in)) {
                    System.out.println("File Content:");
                    while (scanner.hasNextLine()) {
                        System.out.println(scanner.nextLine());
                    }
                }

                // 关闭通道和会话
                channelExec.disconnect();
                session.disconnect();
                System.out.println("Disconnected from " + host);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



}
