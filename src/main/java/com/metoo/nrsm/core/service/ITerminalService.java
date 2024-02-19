package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.entity.nspm.Terminal;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ITerminalService {

    Terminal selectObjById(Long id);

    Page<Terminal> selectConditionQuery(TerminalDTO instance);

    List<Terminal> selectObjByMap(Map params);

    int save(Terminal instance);

    int update(Terminal instance);

    int delete(Long id);

    int batchInert(List<Terminal> instances);

    int batchUpdate(List<Terminal> instances);

    // 同步DT信息
    void syncMacDtToTerminal();

    // 同步终端类型
    void syncHistoryMac(Date time);

    // 改为资产终端
    int editTerminalType(Long[] ids);
}
