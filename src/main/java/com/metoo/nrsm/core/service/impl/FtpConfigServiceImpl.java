package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.FtpConfigMapper;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FtpConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class FtpConfigServiceImpl {

    @Resource
    private FtpConfigMapper ftpConfigMapper;

    /**
     * 保存FTP配置
     */
    @Transactional
    public Result createFtpConfig(FtpConfig config) {
        if (config.getUserName() == null) {
            return ResponseUtil.error("用户名不能为空");
        }
        if (config.getPassword() == null) {
            return ResponseUtil.error("密码不能为空");
        }

        config.setCreateTime(new Date());

        if (ftpConfigMapper.selectById(config.getId()) != null) {
            config.setUpdateTime(new Date());
            int i = this.ftpConfigMapper.update(config);
            if (i >= 1) {
                return ResponseUtil.ok();
            }
        } else {
            int i = this.ftpConfigMapper.insert(config);
            if (i >= 1) {
                return ResponseUtil.ok();
            }
        }
        return ResponseUtil.saveError();
    }

    /**
     * 更新FTP配置
     */
    @Transactional
    public void updateFtpConfig(FtpConfig config) {
        ftpConfigMapper.update(config);
    }


    public Result selectAllQuery() {

        List<FtpConfig> configs = ftpConfigMapper.selectAllActive();
        return ResponseUtil.ok(configs);
    }

}