package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalCount;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TerminalCountMapper {

    TerminalCount selectObjByMap(Map params);

    TerminalCount selectHistoryObjByMap(Map params);

    int save(TerminalCount instance);

    int update(TerminalCount instance);

}
