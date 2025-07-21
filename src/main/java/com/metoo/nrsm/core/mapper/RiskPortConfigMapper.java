package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.RiskPortConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RiskPortConfigMapper {
    // 新增
    @Insert("INSERT INTO metoo_risk_port_config(port_value) VALUES(#{portValue})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RiskPortConfig config);
    

    // 修改
    @Update("UPDATE metoo_risk_port_config SET port_value = #{portValue} WHERE id = #{id}")
    int update(RiskPortConfig config);
    
    // 查询全部
    @Select("SELECT id, port_value AS portValue FROM metoo_risk_port_config")
    List<RiskPortConfig> selectAll();

}