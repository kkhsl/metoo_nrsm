package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.TerminalDiagnosis;
import org.apache.ibatis.annotations.Param;

public interface TerminalDiagnosisMapper {

    TerminalDiagnosis selectObjByType(@Param("type") Integer type);
}
