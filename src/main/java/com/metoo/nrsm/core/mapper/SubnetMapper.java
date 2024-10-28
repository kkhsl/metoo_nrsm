package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Subnet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SubnetMapper {

    Subnet selectObjById(Long id);

    Subnet selectObjByIp(String ip);

    Subnet selectObjByIpAndMask(@Param("ip") String ip, @Param("mask") Integer mask);

    List<Subnet> selectSubnetByParentId(@Param("parentId") Long parentId);

    List<Subnet> selectSubnetByParentIp(Long ip);

    List<Subnet> selectObjByMap(Map params);

    int save(Subnet instance);

    int update(Subnet instance);

    int delete(Long id);

    int deleteTable();

}
