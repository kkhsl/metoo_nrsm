package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalCount;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ITerminalCountService {

    TerminalCount selectHistoryObjByMap(Map params);

    boolean save(TerminalCount instance);

}
