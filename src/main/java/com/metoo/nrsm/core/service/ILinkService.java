package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.LinkDTO;
import com.metoo.nrsm.entity.Link;

import java.util.List;
import java.util.Map;

public interface ILinkService {

    Link selectObjById(Long id);

    Page<Link> selectObjConditionQuery(LinkDTO instance);

    List<Link> selectObjByMap(Map params);

    int save(Link instace);

    int update(Link instace);

    int delete(Long id);

    int batchesDel(Long[] ids);

    int batchesInsert(List<Link> instances);
}
