package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.entity.Unbound;

import java.util.List;
import java.util.Map;

public interface IUnboundService {

    Unbound selectObjByOne(Map params);
    boolean save(Unbound instance);
    boolean update(UnboundDTO instance);
    boolean delete(Long id);

}
