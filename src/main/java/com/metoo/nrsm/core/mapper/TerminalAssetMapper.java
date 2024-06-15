package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.TerminalAssetDTO;
import com.metoo.nrsm.entity.TerminalAsset;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TerminalAssetMapper {

    TerminalAsset selectObjById(Long id);

    List<TerminalAsset> selectObjByConditionQuery(TerminalAssetDTO instance);

    List<TerminalAsset> selectObjByMap(Map params);

    int save(TerminalAsset instance);

    int update(TerminalAsset instance);

    int delete(Long id);

}
