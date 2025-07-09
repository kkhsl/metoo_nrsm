package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Route6Entry;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface Route6TableMapper {

    void batchInsertRoutes(@Param("routes") List<Route6Entry> routes);

    // 按设备IP删除路由
    @Delete("DELETE FROM metoo_route6_table WHERE deviceIp = #{deviceIp}")
    void deleteByDeviceIp(@Param("deviceIp") String deviceIp);


    List<Route6Entry> selectObjByDeviceUuid(String deviceIp);


    void copyDataToRoute6History();
}