package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.OperationLogDTO;
import com.metoo.nrsm.core.mapper.OperationLogMapper;
import com.metoo.nrsm.core.service.IOperationLogService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.entity.OperationLog;
import com.metoo.nrsm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2023-11-07 16:14
 */
@Service
@Transactional
public class IOperationLogServiceImpl implements IOperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Autowired
    private IUserService userService;


    @Override
    public OperationLog selectObjById(Long id) {
        return this.operationLogMapper.selectObjById(id);
    }

    @Override
    public Page<OperationLog> selectObjConditionQuery(OperationLogDTO instance) {
        if (instance == null) {
            instance = new OperationLogDTO();
        }
        Page<OperationLog> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.operationLogMapper.selectObjConditionQuery(instance);
        return page;
    }

    @Override
    public List<OperationLog> selectObjByMap(Map params) {
        return this.operationLogMapper.selectObjByMap(params);
    }

    @Override
    public boolean save(OperationLog instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setAddTime(new Date());
            User user = ShiroUserHolder.currentUser();
            instance.setAccount(user.getUsername());
        }
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.operationLogMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                this.operationLogMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean saveLoginLog(OperationLog instance) {
        try {
            if (instance.getAccount() != null && !instance.getAccount().equals("")) {
                User user = this.userService.findByUserName(instance.getAccount());
                instance.setAddTime(new Date());
//                InetAddress inetAddress = InetAddress.getLocalHost();
//                instance.setIp(inetAddress.getHostAddress());
                instance.setAction("登录");
                instance.setType(2);
            }
            this.operationLogMapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean saveOperationLog(OperationLog instance) {
        try {
            if (instance.getAccount() != null && !instance.getAccount().equals("")) {
                User user = this.userService.findByUserName(instance.getAccount());
                instance.setAddTime(new Date());
                instance.setAction("操作");
                instance.setType(0);
            }
            this.operationLogMapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(OperationLog instance) {
        try {
            this.operationLogMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            this.operationLogMapper.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
