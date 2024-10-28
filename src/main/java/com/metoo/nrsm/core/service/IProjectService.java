package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.ProjectDTO;
import com.metoo.nrsm.entity.Project;

import java.util.List;
import java.util.Map;

public interface IProjectService {

    Project selectObjById(Long id);

    Project selectObjByName(String name);

    Page<Project> selectObjConditionQuery(ProjectDTO dto);

    List<Project> selectObjByMap(Map params);

    int save(Project instance);

    int update(Project instance);

    int delete(Long id);
}
