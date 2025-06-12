package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.TerminalDiagnosis;

public interface ITerminalDiagnosisService {

    TerminalDiagnosis selectObjByType(Integer type);

}
