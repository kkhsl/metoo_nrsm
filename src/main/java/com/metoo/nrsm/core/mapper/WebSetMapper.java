package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.WebSet;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WebSetMapper {
    /**
     * 获取当前网站配置
     */
    @Select("SELECT * FROM metoo_web_set ORDER BY id DESC LIMIT 1")
    WebSet getCurrentConfig();

    /**
     * 更新网站Logo
     * @param logoUrl 新的Logo路径
     */
    @Update("UPDATE metoo_web_set SET logo_url = #{logoUrl}, updated_at = NOW() WHERE id = #{id}")
    int updateLogo(@Param("id") Long id, @Param("logoUrl") String logoUrl);

    @Update("UPDATE metoo_web_set SET name = #{name}, updated_at = NOW() WHERE id = #{id}")
    int updateName(WebSet webSet);


    /**
     * 创建新的网站配置
     * @param webSet 网站配置对象
     */
    @Insert("INSERT INTO metoo_web_set (logo_url) VALUES (#{logoUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WebSet webSet);
    @Insert("INSERT INTO metoo_web_set (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertName(WebSet webSet);


    /**
     * 获取所有历史配置（按时间倒序）
     */
    @Select("SELECT id,name,logo_url as logoUrl FROM metoo_web_set ORDER BY updated_at DESC")
    List<WebSet> getAllConfigs();

}