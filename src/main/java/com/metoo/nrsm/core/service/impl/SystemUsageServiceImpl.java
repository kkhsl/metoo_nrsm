package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.SystemUsageMapper;
import com.metoo.nrsm.core.service.ISystemUsageService;
import com.metoo.nrsm.core.utils.system.SystemUsageUtils;
import com.metoo.nrsm.core.vo.SystemUsageVO;
import com.metoo.nrsm.entity.Disk;
import com.metoo.nrsm.entity.SystemUsage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.util.Util;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);  // 等待 1 秒钟以便获取负载变化
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        cpuLoad = new BigDecimal(cpuLoad).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  // 保留两位小数

        double memUsage = SystemUsageUtils.getMemUsagePercentage();
        memUsage = new BigDecimal(memUsage).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  // 保留两位小数


        SystemUsage systemUsage = new SystemUsage();
        systemUsage.setAddTime(new Date());
        systemUsage.setCpu_usage(cpuLoad);
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
    public List<SystemUsageVO> selectObjVOByMap(Map params) {
        return this.systemUsageMapper.selectObjVOByMap(params);
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
