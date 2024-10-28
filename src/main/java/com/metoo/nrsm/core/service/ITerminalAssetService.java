package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.TerminalAssetDTO;
import com.metoo.nrsm.entity.TerminalAsset;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ITerminalAssetService {

    TerminalAsset selectObjById(Long id);

    Page<TerminalAsset> selectObjByConditionQuery(TerminalAssetDTO instance);

    List<TerminalAsset> selectObjByMap(Map params);

    boolean save(TerminalAsset instance);

    boolean update(TerminalAsset instance);

    int delete(Long id);

}
