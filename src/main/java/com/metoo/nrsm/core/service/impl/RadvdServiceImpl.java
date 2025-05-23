package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.RadvdDTO;
import com.metoo.nrsm.core.mapper.RadvdMapper;
import com.metoo.nrsm.core.service.IRadvdService;
import com.metoo.nrsm.core.system.conf.radvd.service.AbstractRadvdService;
import com.metoo.nrsm.core.system.conf.radvd.service.LinuxRadvdService;
import com.metoo.nrsm.core.system.conf.radvd.service.WindowsRadvdService;
import com.metoo.nrsm.entity.Radvd;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Transactional
public class RadvdServiceImpl implements IRadvdService {

    @Resource
    private RadvdMapper radvdMapper;

    private final Lock lock = new ReentrantLock(); // 用于配置文件操作的锁

    @Override
    public Radvd selectObjById(Long id) {
        return this.radvdMapper.selectObjById(id);
    }

    @Override
    public Radvd selectObjByName(String name) {
        return this.radvdMapper.selectObjByName(name);
    }

    @Override
    public List<Radvd> selectObjByMap(Map params) {
        return this.radvdMapper.selectObjByMap(params);
    }

    @Override
    public Page<Radvd> selectObjConditionQuery(RadvdDTO instance) {
        if(instance == null){
            instance = new RadvdDTO();
        }
        Page<Radvd> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.radvdMapper.selectObjConditionQuery(instance);
        return page;
    }

    @Override
    public boolean save(Radvd instance) {
        lock.lock();  // 获取锁，确保配置文件的原子性操作

        if(instance.getId() == null){
            instance.setAddTime(new Date());
            try {
               this.radvdMapper.save(instance);
               updateRadvdConfFile(); // 更新配置文件
               return true;
            } catch (Exception e) {
                e.printStackTrace();
               return false;
            }finally {
                lock.unlock();
            }
        }else{
            try {
                instance.setUpdateTime(new Date());
                this.radvdMapper.update(instance);
                updateRadvdConfFile(); // 更新配置文件
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }finally {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean update(Radvd instance) {
        lock.lock();  // 获取锁，确保配置文件的原子性操作
        try {
            instance.setUpdateTime(new Date());
            this.radvdMapper.update(instance);
            updateRadvdConfFile(); // 更新配置文件
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public boolean delete(Long id) {
        lock.lock();  // 获取锁，确保配置文件的原子性操作
        try {
            this.radvdMapper.delete(id);
            updateRadvdConfFile(); // 更新配置文件
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            lock.unlock();
        }
    }


    private static final String CONFIG_FILE_PATH = "/etc/radvd.conf"; // 配置文件路径

    // 更新配置文件内容
    private void updateRadvdConfFile() {
        List<Radvd> radvdList = this.radvdMapper.selectObjByMap(Collections.emptyMap());
        // 根据平台选择对应的服务实现
        AbstractRadvdService service;
        if (isLinux()) {
            service = new LinuxRadvdService();
        } else {
            service = new WindowsRadvdService();
        }
        // 更新配置文件
        service.updateConfigFile(radvdList);
    }

    private static boolean isLinux() {
        // 检查系统平台
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

}
