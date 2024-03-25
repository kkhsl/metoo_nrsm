package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.CredentialDTO;
import com.metoo.nrsm.entity.Credential;

import java.util.List;

public interface ICredentialService {

    Credential getObjById(Long id);

    Credential getObjByName(String name);

    List<Credential> query();

    int save(Credential instance);

    int update(Credential instance);

    int delete(Long id);

    int batchesDel(Long[] ids);

//    Map<String, String> getUuid(TopoCredentialDto dto);

    Page<Credential> getObjsByLevel(Credential instance);

    List<Credential> getAll();

    Page<Credential> selectObjByConditionQuery(CredentialDTO dto);


}
