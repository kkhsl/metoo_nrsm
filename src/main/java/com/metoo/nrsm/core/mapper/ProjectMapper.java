package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.ProjectDTO;
import com.metoo.nrsm.entity.Project;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProjectMapper {

    Project selectObjById(Long id);

    Project selectObjByName(String name);

    List<Project> selectConditionQuery(ProjectDTO dto);

    List<Project> selectObjByMap(Map params);

    int save(Project instance);

    int update(Project instance);

    int delete(Long id);

}
