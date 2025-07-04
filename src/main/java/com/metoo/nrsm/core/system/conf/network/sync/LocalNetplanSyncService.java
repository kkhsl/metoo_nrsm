package com.metoo.nrsm.core.system.conf.network.sync;

import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.entity.Interface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LocalNetplanSyncService {

    @Autowired
    private NetplanParserService parserService;

    @Autowired
    private IInterfaceService interfaceService;

//    private static final String NETPLAN_FILE = "/etc/netplan/00-installer-config.yaml";

    @Value("${network.netplan.file}")
    private String NETPLAN_FILE;

    @Transactional
    public void syncInterfaces() {
        try (FileInputStream fis = new FileInputStream(new File(NETPLAN_FILE))) {
            // 1. 解析配置
            List<Interface> interfaces = parserService.parseNetplanConfig(fis);

            // 2. 统一保存到数据库
            saveAllInterfaces(interfaces);

        } catch (Exception e) {
            log.info("同步失败信息：{}", e.getMessage());
            throw new RuntimeException("同步网络接口配置失败", e);
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
            params.put("parentIdNull", true);
            List<Interface> intf = this.interfaceService.selectObjByMap(params);
            if (intf.size() <= 0) {
                interfaceService.save(anInterface);
            }
        }
    }

}