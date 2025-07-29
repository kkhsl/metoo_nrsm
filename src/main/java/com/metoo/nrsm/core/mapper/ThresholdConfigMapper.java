package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.ThresholdConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ThresholdConfigMapper {
    // 新增
    @Insert("INSERT INTO metoo_threshold_value(num) VALUES(#{num})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ThresholdConfig config);
    

    // 修改
    @Update("UPDATE metoo_threshold_value SET num = #{num} WHERE id = #{id}")
    int update(ThresholdConfig config);
    
    // 查询全部
    @Select("SELECT *  FROM metoo_threshold_value")
    List<ThresholdConfig> selectAll();

}