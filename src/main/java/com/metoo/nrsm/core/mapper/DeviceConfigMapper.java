package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.DeviceConfigDTO;
import com.metoo.nrsm.entity.DeviceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceConfigMapper {

    /**
     * 插入设备配置
     * @param config 设备配置对象
     * @return 影响行数
     */
    int insertDeviceConfig(DeviceConfig config);
    
    /**
     * 根据名称检查配置是否存在
     * @param name 配置名称
     * @return 存在数量
     */
    Integer countByName(@Param("name") String name);
    
    /**
     * 批量插入设备配置
     * @param configs 配置列表
     * @return 影响行数
     */
    int batchInsertDeviceConfigs(@Param("list") List<DeviceConfig> configs);

    List<DeviceConfig> selectAll(DeviceConfigDTO instance);

    /**
     * 根据ID列表查询备份记录
     */
    List<DeviceConfig> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID列表删除备份记录
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    DeviceConfig selectById(Long id);


}