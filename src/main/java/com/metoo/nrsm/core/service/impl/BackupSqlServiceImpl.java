package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.BackupSqlDTO;
import com.metoo.nrsm.core.mapper.BackupSqlMapper;
import com.metoo.nrsm.core.service.IBackupSqlService;
import com.metoo.nrsm.entity.BackupSql;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BackupSqlServiceImpl implements IBackupSqlService {

    @Resource
    private BackupSqlMapper backupSqlMapper;

    @Override
    public BackupSql selectObjById(Long id) {
        return this.backupSqlMapper.selectObjById(id);
    }

    @Override
    public BackupSql selectObjByName(String name) {
        return this.backupSqlMapper.selectObjByName(name);
    }

    @Override
    public Page<BackupSql> selectObjConditionQuery(BackupSqlDTO dto) {
        if(dto == null){
            dto = new BackupSqlDTO();
        }
        Page<BackupSql> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.backupSqlMapper.selectObjConditionQuery(dto);
        return page;
    }

    @Override
    public List<BackupSql> selectObjByMap(Map params) {
        return this.backupSqlMapper.selectObjByMap(params);
    }

    @Override
    public int save(BackupSql instance) {
        if(instance.getId() == null){
            instance.setAddTime(new Date());
        }

        if(instance.getId() == null || instance.getId().equals("")){
            try {
                return this.backupSqlMapper.save(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {
                return this.backupSqlMapper.update(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    public int update(BackupSql instance) {
        try {
            return this.backupSqlMapper.update(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            return this.backupSqlMapper.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
