package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.BackupSqlDTO;
import com.metoo.nrsm.entity.BackupSql;

import java.util.List;
import java.util.Map;

public interface IBackupSqlService {

    BackupSql selectObjById(Long id);

    BackupSql selectObjByName(String name);

    Page<BackupSql> selectObjConditionQuery(BackupSqlDTO dto);

    List<BackupSql> selectObjByMap(Map params);

    int save(BackupSql instance);

    int update(BackupSql instance);

    int delete(Long id);

}
