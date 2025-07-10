package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.RouteEntry;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

@Mapper
public interface RouteTableMapper {

    void batchInsertRoutes(@Param("routes") List<RouteEntry> routes);

    // 按设备IP删除路由
    @Delete("DELETE FROM metoo_route_table WHERE deviceIp = #{deviceIp}")
    void deleteByDeviceIp(@Param("deviceIp") String deviceIp);


    List<RouteEntry> selectObjByDeviceUuid(String deviceIp, Date time);

    void copyDataToRouteHistory();


}