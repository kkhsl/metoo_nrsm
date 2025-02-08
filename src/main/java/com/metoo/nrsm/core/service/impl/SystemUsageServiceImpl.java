package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.SystemUsageMapper;
import com.metoo.nrsm.core.service.ISystemUsageService;
import com.metoo.nrsm.core.utils.system.SystemUsageUtils;
import com.metoo.nrsm.entity.Disk;
import com.metoo.nrsm.entity.SystemUsage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SystemUsageServiceImpl implements ISystemUsageService {

    @Resource
    private SystemUsageMapper systemUsageMapper;

    @Override
    public void saveSystemUsageToDatabase() {
        double cpuUsage = SystemUsageUtils.getCpuUsagePercentage();
        double memUsage = SystemUsageUtils.getMemUsagePercentage();

        SystemUsage systemUsage = new SystemUsage();
        systemUsage.setAddTime(new Date());
        systemUsage.setCpu_usage(cpuUsage);
        systemUsage.setMem_usage(memUsage);

        try {
            this.save(systemUsage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<SystemUsage> selectObjByMap(Map params) {
        return this.systemUsageMapper.selectObjByMap(params);
    }

    @Override
    public boolean save(SystemUsage instance) {
        try {
            this.systemUsageMapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
