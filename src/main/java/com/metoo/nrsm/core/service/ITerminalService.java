package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.entity.Terminal;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ITerminalService {

    Terminal selectObjById(Long id);

    Page<Terminal> selectObjByConditionQuery(TerminalDTO instance);

    List<Terminal> selectObjByMap(Map params);

    List<Terminal> selectObjHistoryByMap(Map params);

    boolean save(Terminal instance);

    boolean update(Terminal instance);

    int delete(Long id);

    boolean deleteObjByType(Integer type);

    boolean batchSave(List<Terminal> instance);

    boolean batchUpdate(List<Terminal> instance);

    void syncTerminal(Date date);

    void v4Tov6Terminal(Date date);

    void writeTerminalUnit();

    void writeTerminalUnitV6();

    void dualStackTerminal();

    void writeTerminalType();

    void syncTerminalToTerminalHistory();

    Map<String, Integer> terminalCount();

}
