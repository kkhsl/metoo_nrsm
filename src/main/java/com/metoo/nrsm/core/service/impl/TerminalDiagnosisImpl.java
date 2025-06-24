package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.TerminalDiagnosisMapper;
import com.metoo.nrsm.core.service.ITerminalDiagnosisService;
import com.metoo.nrsm.entity.TerminalDiagnosis;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class TerminalDiagnosisImpl implements ITerminalDiagnosisService {

    @Resource
    private TerminalDiagnosisMapper terminalDiagnosisMapper;

    @Override
    public TerminalDiagnosis selectObjByType(Integer type) {
        return this.terminalDiagnosisMapper.selectObjByType(type);
    }
}
