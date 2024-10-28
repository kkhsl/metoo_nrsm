package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.BackupSqlDTO;
import com.metoo.nrsm.entity.BackupSql;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

public interface BackupSqlMapper {

    BackupSql selectObjById(Long id);

    BackupSql selectObjByName(String name);

    List<BackupSql> selectObjConditionQuery(BackupSqlDTO dto);

    List<BackupSql> selectObjByMap(Map params);

    int save(BackupSql instance);

    int update(BackupSql instance);

    int delete(Long id);
}
